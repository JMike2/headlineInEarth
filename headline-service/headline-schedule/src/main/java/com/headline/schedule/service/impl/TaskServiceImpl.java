package com.headline.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.headline.common.constants.ScheduleConstants;
import com.headline.common.redis.CacheService;
import com.headline.model.schedule.dtos.Task;
import com.headline.model.schedule.pojos.Taskinfo;
import com.headline.model.schedule.pojos.TaskinfoLogs;
import com.headline.schedule.mapper.TaskinfoLogsMapper;
import com.headline.schedule.mapper.TaskinfoMapper;
import com.headline.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Override
    public long addTask(Task task) {
        //添加任务到数据库中
        boolean success = addTaskToDb(task);
        //添加任务到redis中
        if(success){
            addTaskToCache(task);
        }


        return task.getTaskId();
    }



    @Autowired
    private CacheService cacheService;

    private void addTaskToCache(Task task) {
        String key = task.getTaskType()+"_"+task.getPriority();
        //获取五分钟之后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);
        long nextScheduleTime = calendar.getTimeInMillis();
        //如果任务执行时间小于等于当前时间，存入list
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        } else if (task.getExecuteTime()<=nextScheduleTime) {
            //如果任务执行时间大于当前时间&&小于五分钟 存入Zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),task.getExecuteTime());
        }


    }

    /**
     * 添加任务到数据库中
     * @param task
     * @return
     */
    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    private boolean addTaskToDb(Task task) {
        boolean flag = false;
        try{
            //保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);
            //设置task   id
            task.setTaskId(taskinfo.getTaskId());
            //保存任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;

    }
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        //删除任务，变更任务日志
        Task task= updateDb(taskId,ScheduleConstants.CANCELLED);
        //删除redis数据
        if(task!=null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 删除redis中的数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType()+"_"+task.getPriority();
        if(task.getExecuteTime()<=System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else {
            cacheService.zRemove(ScheduleConstants.FUTURE+key,JSON.toJSONString(task));
        }
    }

    /**
     * 删除任务，更新任务日志
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());


        }catch (Exception e){
            log.error("task cancel exception taskId={}",taskId);
        }
        return task;
    }

    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try{
            //从redis中拉取数据 pop
            String key = type+"_"+priority;
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if(StringUtils.isNotBlank(task_json)){
                 task = JSON.parseObject(task_json, Task.class);
                //修改数据库信息
                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("poll task exception");
        }
        return task;
    }

    /**
     * 未来数据定时刷新
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新--定时任务");
            //获取所有未来数据集合key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            //按照key和分值查询符合查询条件的数据
            for(String futureKey : futureKeys){
                //获取当前数据的key topic
                String topicKey = ScheduleConstants.TOPIC+futureKey.split(ScheduleConstants.FUTURE)[1];
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                //同步数据
                if(!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(futureKey,topicKey,tasks);
                    log.info("成功将"+futureKey+"刷新到"+topicKey);
                }
            }
        }
    }

    /**
     * 数据库任务定时同步到redis中
     */
    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){
        //清理缓存中的数据
        clearCache();
        //查询符合条件的任务 小于等于未来5分钟的数据
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE,5);

        List<Taskinfo> taskinfoList = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));

        //把任务添加到redis
        if(taskinfoList!=null && taskinfoList.size()>0){
            for (Taskinfo taskinfo : taskinfoList) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo,task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }
        log.info("数据库任务同步到了redis");
    }
    public void clearCache(){
        Set<String> topicKey = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKey = cacheService.scan(ScheduleConstants.FUTURE +"*");
        cacheService.delete(topicKey);
        cacheService.delete(futureKey);
    }
}
