package com.headline.apis.schedule;

import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("headline-schedule")

public interface IScheduleClient {
    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    @PostMapping("api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task);

    /**
     * 取消延迟任务
     * @param taskId
     * @return
     */
    @GetMapping("api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId);

    /**
     * 按照类型和优先级拉去任务
     * @param type
     * @param priority
     * @return
     */
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type,@PathVariable("priority") int priority);
}
