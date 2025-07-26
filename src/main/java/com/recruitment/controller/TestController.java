package com.recruitment.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/cors")
    public Map<String, Object> testCors(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful");
        response.put("origin", request.getHeader("Origin"));
        response.put("serverPort", request.getServerPort());
        return response;
    }
}
