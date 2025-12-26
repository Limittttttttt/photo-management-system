package org.example.picmanager.config;

import com.baidu.aip.imageclassify.AipImageClassify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Value("${baidu.ai.app-id}")
    private String appId;

    @Value("${baidu.ai.api-key}")
    private String apiKey;

    @Value("${baidu.ai.secret-key}")
    private String secretKey;

    @Bean
    public AipImageClassify aipImageClassify() {
        AipImageClassify client = new AipImageClassify(appId, apiKey, secretKey);
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        return client;
    }
}
