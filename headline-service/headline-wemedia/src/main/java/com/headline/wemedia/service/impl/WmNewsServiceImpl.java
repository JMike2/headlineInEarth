package com.headline.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.headline.common.constants.WemediaConstants;
import com.headline.common.exception.CustomException;
import com.headline.model.common.dtos.PageResponseResult;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.common.enums.AppHttpCodeEnum;
import com.headline.model.wemedia.dtos.WmNewsDto;
import com.headline.model.wemedia.dtos.WmNewsPageReqDto;
import com.headline.model.wemedia.pojos.WmMaterial;
import com.headline.model.wemedia.pojos.WmNews;
import com.headline.model.wemedia.pojos.WmNewsMaterial;
import com.headline.utils.thread.WmTreadLocalUtil;
import com.headline.wemedia.mapper.WmMaterialMapper;
import com.headline.wemedia.mapper.WmNewsMapper;
import com.headline.wemedia.mapper.WmNewsMaterialMapper;
import com.headline.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl  extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {


    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        //检查参数
        dto.checkParam();
        //分页条件查询
        IPage page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //状态精确查询
        if(dto.getStatus() !=null){
            lambdaQueryWrapper.eq(WmNews::getStatus,dto.getStatus());
        }
        //频道精确查询
        if(dto.getChannelId()!=null){
            lambdaQueryWrapper.eq(WmNews::getChannelId,dto.getChannelId());
        }
        //时间范围查询
        if(dto.getBeginPubDate()!=null&&dto.getEndPubDate()!=null){
            lambdaQueryWrapper.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        //关键字模糊查询
        if(StringUtils.isNotBlank(dto.getKeyword())){
            lambdaQueryWrapper.like(WmNews::getTitle,dto.getKeyword());
        }
        //查询当前登陆人的文章
        lambdaQueryWrapper.eq(WmNews::getUserId, WmTreadLocalUtil.getUser().getId());
        //按照发布时间倒叙查询
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);
        page(page,lambdaQueryWrapper);
        //结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //条件判断
        if(dto==null||dto.getContent() == null){
            return  ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //保存或修改文章
        WmNews wmNews = new WmNews();
        //属性拷贝
        BeanUtils.copyProperties(dto,wmNews);
        //封面图片
        if(dto.getImages() !=null && dto.getImages().size()>0){
            String imageStr = org.apache.commons.lang.StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(imageStr);
        }
        //如果当前封面类型自动为-1
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }
        saveOrUpdateWmNews(wmNews);
        //判断是否为草稿，如果是草稿结束当前方法
        if(dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        //不是草稿，保存文章内容图片与素材的关系
        //获取文章中的图片信息
        List<String> materials = ectractUrlInfo(dto.getContent());
        saveRelativeInfoForContent(materials,wmNews.getId());
        //不是草稿，保存文章封面图片与素材的关系,如果当前布局是自动，需要匹配封面图片
        saveRelativeInfoForCover(dto,wmNews,materials);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 如果封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1.如果内容图片大于等于1，小于3 单图 type 1
     * 2.如果内容图片大于等于3 多图 type 3
     * 3.如果没有图片，无图 type 0
     *
     * 保存封面图片与素材的关系
     * @param dto
     * @param wmNews
     * @param materials
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        List<String> images = dto.getImages();
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            //多图
            if(materials.size()>=3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size() >= 1&& materials.size()<3) {
                //单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            }else {
                //无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            //修改文章
            if(images != null && images.size()>0){
                wmNews.setImages(org.apache.commons.lang.StringUtils.join(images,","));
            }
            updateById(wmNews);

        }
        if(images != null && images.size()>0){
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 处理文章内容图片和素材的关系
     * @param materials
     * @param id
     */
    private void saveRelativeInfoForContent(List<String> materials, Integer id) {
        saveRelativeInfo(materials,id,WemediaConstants.WM_CONTENT_REFERENCE);
    }
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    /**
     * 保存文章图片与素材的关系到数据库中
     * @param materials
     * @param id
     * @param type
     */

    private void saveRelativeInfo(List<String> materials, Integer id, Short type) {
        if(materials!=null && !materials.isEmpty()){
            //通过图片的url查询图片id
            List<WmMaterial> dbMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl, materials));
            //判断素材是否有效
            if(dbMaterials==null||dbMaterials.size()==0){
                //手动抛出异常 第一个功能：能够提示调用者素材失效， 第二个功能进行素材回滚
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            if(materials.size()!=dbMaterials.size()){
                throw  new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            List<Integer> idList = dbMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            //批量保存
            wmNewsMaterialMapper.saveRelations(idList,id,type);
        }

    }


    /**
     * 提取文章内容中的图片信息
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if(map.get("type").equals("image")){
                String imgUrl = (String) map.get("value");
                materials.add(imgUrl);
            }
        }
        return materials;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    private void saveOrUpdateWmNews(WmNews wmNews){
        //补全属性
        wmNews.setUserId(WmTreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime( new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架
        if(wmNews.getId() == null){
            //保存
            save(wmNews);
        }else{
            //修改
            //删除文章图片和素材的关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }
}
