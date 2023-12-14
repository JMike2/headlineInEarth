package com.headline.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.headline.model.article.dtos.ArticleHomeDto;
import com.headline.model.article.pojos.ApArticle;
import com.headline.model.common.dtos.ResponseResult;

public interface ApArticleService extends IService<ApArticle> {
     ResponseResult load(ArticleHomeDto dto,Short type);
}
