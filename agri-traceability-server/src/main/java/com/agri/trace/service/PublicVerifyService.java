package com.agri.trace.service;

import com.agri.trace.vo.PublicVerifyVO;
import jakarta.servlet.http.HttpServletRequest;

public interface PublicVerifyService {
    PublicVerifyVO verify(String certificateCode, HttpServletRequest request);
}
