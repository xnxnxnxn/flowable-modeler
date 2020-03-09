package com.shareniu.flowablemodeler7.filter;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author myb
 *         Created by myb on 2016/6/1.
 */
@Component
public class CorsFilter implements Filter {

    private String server = "*";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;

        // 跨域配置
        response.setHeader("Access-Control-Allow-Origin", server);
        // 允许方法配置
//        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        // 预检时间间隔30分钟 （http:options）
        response.setHeader("Access-Control-Max-Age", "86400");
        // 取消缓存
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store");
        // Http头部信息
        response.setHeader("Access-Control-Allow-Headers",
                "x-requested-with, Api-Ver, Authorization, L-Authorization, locale, accept, content-type, x-http-method-override, Keep-Time, Cache-Control, Expires, Pragma");

        ServletRequest requestWrapper = null;

        chain.doFilter(req, response);

    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
