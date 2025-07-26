//package com.recruitment.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.recruitment.service.OpenAIService;
//
//@CrossOrigin(origins = "https://1c.atract.in/")
//@RestController
//@RequestMapping("/api/jd")
//public class JDController {
//
//    @Autowired
//    private OpenAIService openAiService;
//
//    @GetMapping("/generate")
//    public ResponseEntity<String> generateJD(@RequestParam String title) {
//        System.out.println("this is openAIController");
//        String jd = openAiService.generateJD(title);
//        return ResponseEntity.ok(jd);
//    }
//}
