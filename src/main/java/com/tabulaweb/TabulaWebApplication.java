package com.tabulaweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

@SpringBootApplication
public class TabulaWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(TabulaWebApplication.class, args);
    }
 @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // حدد حجم الملف الواحد الأقصى (مثال: 50 ميجابايت)
        factory.setMaxFileSize(DataSize.ofMegabytes(50));
        
        // حدد حجم الطلب الكلي الأقصى للملفات الأربعة معاً (مثال: 200 ميجابايت)
        factory.setMaxRequestSize(DataSize.ofMegabytes(200));
        
        return factory.createMultipartConfig();
    }
}
