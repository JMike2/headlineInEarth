package headline.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.headline.model.common.dtos.ResponseResult;
import com.headline.model.user.dtos.LoginDto;
import com.headline.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {
    //登录功能
    public ResponseResult login(LoginDto dto);
}
