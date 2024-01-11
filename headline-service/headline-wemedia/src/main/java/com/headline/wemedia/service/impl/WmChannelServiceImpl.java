package com.headline.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.wemedia.pojos.WmChannel;
import com.headline.wemedia.mapper.WmChannelMapper;
import com.headline.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper, WmChannel> implements WmChannelService {


    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }


}