package com.headline.model.article.dtos;

import com.headline.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class ArticleDto extends ApArticle {

    private String content;
}
