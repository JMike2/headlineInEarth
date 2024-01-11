package com.headline.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.wemedia.dtos.WmNewsDto;
import com.headline.model.wemedia.dtos.WmNewsPageReqDto;
import com.headline.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.RequestBody;

public interface WmNewsService extends IService<WmNews> {
    /**
     * 条件查询文章列表
     * @param dto
     * @return
     */
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto);

    /**
     * 发布文章或保存为草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);
}
