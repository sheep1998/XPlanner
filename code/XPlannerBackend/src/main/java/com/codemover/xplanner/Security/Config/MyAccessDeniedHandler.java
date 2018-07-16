package com.codemover.xplanner.Security.Config;

import com.google.gson.Gson;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("errno", 2);
        hashMap.put("errMsg", "Authorization failed: Permission Denied");
        PrintWriter out = response.getWriter();
        System.out.println("AccessDenied!");
        out.write(new Gson().toJson(hashMap));
        out.flush();
        out.close();
    }

}
