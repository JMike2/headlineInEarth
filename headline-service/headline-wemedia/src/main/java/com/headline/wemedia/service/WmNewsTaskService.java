package com.headline.wemedia.service;

import java.util.Date;

public interface WmNewsTaskService {
    /**
     * 添加任务到延迟队列中
     * @param id
     * @param publishTime
     */
    public void addNewsToTask(Integer id, Date publishTime);

    /**
     * 消费任务
     */
    public void scanNewsByTask();
}
