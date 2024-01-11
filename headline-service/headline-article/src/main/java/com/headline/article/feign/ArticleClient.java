package com.headline.article.feign;

import com.headline.apis.article.IArticleClient;
import com.headline.article.service.ApArticleService;
import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleClient implements IArticleClient {
    @Autowired
    private ApArticleService apArticleService;
    @PostMapping("/api/v1/article/save")
    @Override
    public ResponseResult saveArticle(@RequestBody ArticleDto articleDto) {
        return apArticleService.saveArticle(articleDto);
    }
}
