package com.agri.trace.service;

import com.agri.trace.dto.LoginDTO;
import com.agri.trace.vo.AuthInfoVO;
import com.agri.trace.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);

    AuthInfoVO info();
}
