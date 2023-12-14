package com.headline.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.headline.article.mapper.ApArticleMapper;
import com.headline.article.service.ApArticleService;
import com.headline.common.constants.ArticleConstants;
import com.headline.model.article.dtos.ArticleHomeDto;
import com.headline.model.article.pojos.ApArticle;
import com.headline.model.common.dtos.ResponseResult;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
}
