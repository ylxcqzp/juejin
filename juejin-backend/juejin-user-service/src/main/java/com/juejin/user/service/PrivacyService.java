package com.juejin.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.juejin.user.dto.UpdatePrivacyDTO;
import com.juejin.user.entity.UserPrivacy;
import com.juejin.user.vo.UserProfileVO;

public interface PrivacyService extends IService<UserPrivacy> {

    UserProfileVO.PrivacyVO getPrivacy(Long userId);

    void updatePrivacy(Long userId, UpdatePrivacyDTO dto);

}
