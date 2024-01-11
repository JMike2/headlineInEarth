package com.headline.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.headline.article.mapper.ApArticleConfigMapper;
import com.headline.article.mapper.ApArticleContentMapper;
import com.headline.article.mapper.ApArticleMapper;
import com.headline.article.service.ApArticleService;
import com.headline.common.constants.ArticleConstants;
import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.article.dtos.ArticleHomeDto;
import com.headline.model.article.pojos.ApArticle;
import com.headline.model.article.pojos.ApArticleConfig;
import com.headline.model.article.pojos.ApArticleContent;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.common.enums.AppHttpCodeEnum;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private  ApArticleMapper apArticleMapper;

    private  final  static short MAX_PAGE_SIZE = 50;

    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        //校验参数
        //分页条数校验
        Integer size = dto.getSize();
        if(size==null||size==0){
            size=10;
        }
        size=Math.min(size,50);
        dto.setSize(size);
        //校验参数type
        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type=ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //校验频道参数
        if(StringUtils.isBlank(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        //时间校验
        if(dto.getMaxBehotTime() ==null) dto.setMaxBehotTime(new Date());
        if(dto.getMinBehotTime() ==null) dto.setMinBehotTime(new Date());

        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, type);

        ResponseResult responseResult = ResponseResult.okResult(apArticles);
        return responseResult;
    }
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        //1.检查参数
        if(articleDto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApArticle apArticle =new ApArticle();
        BeanUtils.copyProperties(articleDto,apArticle);
        //2.是否存在id
        if(articleDto.getId()==null){
            //2.1不存在id 保存 文章 文章配置 文章内容
            save(apArticle);
            //保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);
            //保存内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }else {
            //2.2存在 修改 文章 文章内容
            updateById(apArticle);
            //修改内容
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, articleDto.getId()));
            apArticleContent.setContent(articleDto.getContent());
            apArticleContentMapper.updateById(apArticleContent);

        }

        //3.结果返回 文章id
        return ResponseResult.okResult(apArticle.getId());
    }
}
