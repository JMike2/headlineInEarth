package com.headline.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.headline.apis.schedule.IScheduleClient;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.common.enums.TaskTypeEnum;
import com.headline.model.schedule.dtos.Task;
import com.headline.model.wemedia.pojos.WmNews;
import com.headline.utils.common.ProtostuffUtil;
import com.headline.wemedia.service.WmNewsAutoScanService;
import com.headline.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
@Service
@Slf4j
public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private IScheduleClient scheduleClient;
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加任务到延迟队列中------begin");
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        scheduleClient.addTask(task);
        log.info("添加任务到延迟队列中------end");
    }
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Scheduled(fixedRate = 1000)
    @Override
    public void scanNewsByTask() {
        log.info("消费任务");
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if(responseResult.getCode().equals(200) && responseResult.getData()!=null){
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());

        }

    }
}
