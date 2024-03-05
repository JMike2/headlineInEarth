package com.headline.search.service;

import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.search.dtos.UserSearchDto;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public interface ArticleSearchService {
    /**
     * es文章分页检索
     * @param dto
     * @return
     */
    public ResponseResult search( UserSearchDto dto) throws IOException;
}
