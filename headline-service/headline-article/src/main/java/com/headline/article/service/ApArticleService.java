package com.headline.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.article.dtos.ArticleHomeDto;
import com.headline.model.article.pojos.ApArticle;
import com.headline.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

public interface ApArticleService extends IService<ApArticle> {
     ResponseResult load(ArticleHomeDto dto,Short type);

     /**
      * 保存app文章
      * @param articleDto
      * @return
      */
     public ResponseResult saveArticle(ArticleDto articleDto);
}
