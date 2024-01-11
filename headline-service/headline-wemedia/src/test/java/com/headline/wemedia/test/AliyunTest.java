package com.headline.wemedia.test;

import com.headline.common.aliyun.GreenImageScan;
import com.headline.common.aliyun.GreenTextScan;
import com.headline.file.service.FileStorageService;
import com.headline.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {
    /**
     * 测试文本
     */
    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;
    @Test
    public void testScanText() throws Exception{
        Map map = greenTextScan.greeTextScan("我是一个好人,冰毒");
        System.out.println(map);
    }

    /**
     * 测试图片审核
     */
    @Test
    public void testScanImage() throws Exception{
        Map map = greenImageScan.imageScan("http://47.236.111.222:9000/leadnews/2024/01/05/bb48a19becd547e89abbf69fe928648d.jpg");
        System.out.println(map);
    }
}
