package com.headline.apis.article.fallback;

import com.headline.apis.article.IArticleClient;
import com.headline.model.article.dtos.ArticleDto;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class IArticleFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto articleDto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败");
    }
}
