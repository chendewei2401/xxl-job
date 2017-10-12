package com.cmcc.pay.admin.controller.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截, 简易版
 * @author xuxueli 2015-12-12 18:09:04
 */
public class PermissionInterceptor extends HandlerInterceptorAdapter {

	public static final String LOGIN_IDENTITY_KEY = "LOGIN_IDENTITY";


	public static boolean ifLogin(HttpServletRequest request){
        return true;
    }

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (!(handler instanceof HandlerMethod)) {
			return super.preHandle(request, response, handler);
		}

//		if (!ifLogin(request)) {
//			HandlerMethod method = (HandlerMethod)handler;
//			PermessionLimit permission = method.getMethodAnnotation(PermessionLimit.class);
//			if (permission == null || permission.limit()) {
//				response.sendRedirect(request.getContextPath() + "/toLogin");
//				//request.getRequestDispatcher("/toLogin").forward(request, response);
//				return false;
//			}
//		}

		return super.preHandle(request, response, handler);
	}

}
