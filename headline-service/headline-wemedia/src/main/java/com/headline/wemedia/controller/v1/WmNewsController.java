package com.headline.wemedia.controller.v1;

import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.wemedia.dtos.WmNewsDto;
import com.headline.model.wemedia.dtos.WmNewsPageReqDto;
import com.headline.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmNewsPageReqDto dto){
        return wmNewsService.findList(dto);
    }
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        return wmNewsService.submitNews(dto);
    }
}
