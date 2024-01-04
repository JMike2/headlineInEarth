package com.headline.model.wemedia.dtos;

import com.headline.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class WmMaterialDto extends PageRequestDto {
    /**
     * 1.收藏
     * 2.未收藏
     */
    private Short isCollection;
}
