package com.myapp.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("MyApp Server API")
                        .description("MyApp Server 接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("MyApp Team")
                                .email("support@myapp.com")));
    }
}
