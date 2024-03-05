package com.headline.schedule.service.impl;

import com.headline.model.schedule.dtos.Task;
import com.headline.schedule.ScheduleApplication;
import com.headline.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {
    @Autowired
    private TaskService taskService;
    @Test
    public void addTask() {
        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(50);
        task.setParameters("task test".getBytes());
        task.setExecuteTime(new Date().getTime());
        long id = taskService.addTask(task);
        System.out.println(id);
    }

    @Test
    public void cancelTask(){
        taskService.cancelTask(1749488047059169282L);
    }

    @Test
    public void testPoll(){
        Task polled = taskService.poll(100,50);
        System.out.println(polled);
    }
}