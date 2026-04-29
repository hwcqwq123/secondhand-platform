package com.example.secondhand.audit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

// 新增文件：配置调用本地 AI 审核服务的 RestTemplate
@Configuration
public class AuditClientConfig {

    @Bean
    public RestTemplate auditRestTemplate(
            @Value("${app.audit.timeout-ms:5000}") int timeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 新增：连接超时时间
        factory.setConnectTimeout(timeoutMs);

        // 新增：读取超时时间
        factory.setReadTimeout(timeoutMs);

        return new RestTemplate(factory);
    }
}