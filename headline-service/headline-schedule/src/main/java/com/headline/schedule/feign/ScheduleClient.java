package com.headline.schedule.feign;

import com.headline.apis.schedule.IScheduleClient;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.schedule.dtos.Task;
import com.headline.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleClient implements IScheduleClient {
    @Autowired
    private TaskService taskService;

    @PostMapping("api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task){
        return ResponseResult.okResult(taskService.addTask(task));
    }

    @GetMapping("api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId){
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type") int type,@PathVariable("priority") int priority) {
        return ResponseResult.okResult(taskService.poll(type,priority));
    }
}
