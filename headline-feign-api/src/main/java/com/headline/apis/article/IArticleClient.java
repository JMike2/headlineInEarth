package com.headline.apis.article;

import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient("headline-article")
public interface IArticleClient {
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto);

}
