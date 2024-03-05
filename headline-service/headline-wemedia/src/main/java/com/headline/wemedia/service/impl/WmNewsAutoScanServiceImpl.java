package com.headline.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.headline.apis.article.IArticleClient;
import com.headline.common.aliyun.GreenImageScan;
import com.headline.common.aliyun.GreenTextScan;
import com.headline.common.tess4j.Tess4jClient;
import com.headline.file.service.FileStorageService;
import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.wemedia.pojos.WmChannel;
import com.headline.model.wemedia.pojos.WmNews;
import com.headline.model.wemedia.pojos.WmUser;
import com.headline.wemedia.mapper.WmChannelMapper;
import com.headline.wemedia.mapper.WmNewsMapper;
import com.headline.wemedia.mapper.WmUserMapper;
import com.headline.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang.StringUtils;
import org.apache.yetus.audience.InterfaceAudience;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@Async //表明方法是异步方法
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Override
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //从内容中提取文章内容和图片
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);
            //2.审核文章内容
            boolean isTextScan = handTextScan((String) textAndImages.get("content"),wmNews);
            if(!isTextScan){
                return;
            }
            //3.审核图片
            boolean isImageScan= handImageScan((List<String>) textAndImages.get("images"),wmNews);
            if(!isImageScan){
                return;
            }
            //4、成功，保存app端文章数据
            ResponseResult responseResult = SaveAppArticle(wmNews);
            if(!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app相关文章数据失败");
            }
            //回填article_id
            wmNews.setArticleId((Long) responseResult.getData());
            wmNews.setStatus((short)9);
            wmNews.setReason("审核成功");
            wmNewsMapper.updateById(wmNews);
        }

    }
    @Autowired
    private IArticleClient iArticleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    /**
     * 保存app端文章数据
     * @param wmNews
     */
    private ResponseResult SaveAppArticle(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        //文章布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel !=null){
            dto.setChannelName(wmChannel.getName());
        }
        //作者

        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmNews != null){
            dto.setAuthorId(wmNews.getUserId().longValue());
        }
        //设置文章id
        if(wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;


    }
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private Tess4jClient tess4jClient;
    /**
     * 审核图片
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handImageScan(List<String> images, WmNews wmNews)  {
        boolean flag = true;
        if(images == null || images.size()==0) return flag;
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        for (String image : images) {
            //图片识别
            byte[] bytes = fileStorageService.downLoadFile(image);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            String s = null;
            try {
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                s = tess4jClient.doOCR(bufferedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //过滤文字
            boolean isSensitive = handTextScan(s, wmNews);
            if(!isSensitive){
                return isSensitive;
            }
            try {

                Map map = greenImageScan.imageScan(image);
                if(map.get("status").equals("reject")){
                    flag = false;
                    wmNews.setStatus((short)2);
                    wmNews.setReason("当前文章存在违规内容");
                    wmNewsMapper.updateById(wmNews);
                    return flag;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;
    /**
     * 审核纯文本内容
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handTextScan(String content, WmNews wmNews) {
        boolean flag = true;
        if((wmNews.getTitle()+"-"+content).length()==1){
            return flag;
        }
        try {
            Map map = greenTextScan.greeTextScan(wmNews.getTitle()+"-"+content);
            if(map !=null){
                //审核失败
                if(map.get("status").equals("reject")){
                    flag = false;
                    wmNews.setStatus((short)2);
                    wmNews.setReason("当前文章存在违规内容");
                    wmNewsMapper.updateById(wmNews);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 从自媒体文章内容中提取文本和图片
     * 提取封面图片
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        //从自媒体文章内容中提取文本和图片
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if(map.get("type").equals("text")){
                    stringBuilder.append(map.get("value"));
                }
                if(map.get("type").equals("image")){
                    images.add((String) map.get("value"));
                }
            }
        }
        //提取封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("content",stringBuilder.toString());
        resultMap.put("images",images);
        return  resultMap;
    }
}
