package com.myapp.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 设置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射规则
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        // 配置 knife4j 的静态资源请求映射地址
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        // 添加视频资源映射
        registry.addResourceHandler("/videos/**").addResourceLocations("classpath:/static/videos/");
        // 添加图片资源映射
        registry.addResourceHandler("/pic/**").addResourceLocations("classpath:/static/pic/");

    }

    //  添加转换器
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加对 application/x-www-form-urlencoded 的支持
        converters.add(new FormHttpMessageConverter());
    }
}