package com.headline.schedule.test;

import com.headline.common.redis.CacheService;
import com.headline.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {
    @Autowired
    private CacheService cacheService;
    @Test
    public void testList(){
        //在list左边添元素
       // cacheService.lLeftPush("list001","hello,redis");
        //在list右边删除元素
        String list001 = cacheService.lRightPop("list001");
        System.out.println(list001);
    }

    @Test
    public  void testZset(){

    }
}
