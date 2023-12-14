package com.headline.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.headline.article.ArticleApplication;
import com.headline.article.mapper.ApArticleContentMapper;
import com.headline.article.service.ApArticleService;
import com.headline.file.service.FileStorageService;
import com.headline.model.article.pojos.ApArticle;
import com.headline.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleService apArticleService;
    @Test
    public void createStaticUrlTest() throws Exception{
        //获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, "1302977558807060482L"));
        if(apArticleContent!=null && StringUtils.isNotBlank(apArticleContent.getContent())){
            //通过freemarker生成html文件
            Template template = configuration.getTemplate("article.ftl");
            Map<String,Object> content = new HashMap<>();
            //数据模型
            content.put("content", JSONArray.parseArray(apArticleContent.getContent()) );
            StringWriter out = new StringWriter();
            //合成
            template.process(content,out);
            //把html上传到minio中
            InputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);
            //修改ap_article表，添加static_url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId,apArticleContent.getArticleId())
                    .set(ApArticle::getStaticUrl,path));

        }

    }
}
