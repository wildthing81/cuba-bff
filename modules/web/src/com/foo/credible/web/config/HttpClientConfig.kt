package com.foo.credible.web.config

import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import com.anzi.credible.constants.AppConstants

@Configuration
open class HttpClientConfig {

    /**
     * Rest template for event sourcing api access
     *
     * @return RestTemplate
     */
    @Primary
    @Bean
    open fun esRestTemplate(): RestTemplate {
        val config = RequestConfig.custom()
            .setConnectTimeout(AppConstants.ES_CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(AppConstants.ES_CONNECT_REQ_TIMEOUT)
            .setSocketTimeout(AppConstants.ES_SOCKET_TIMEOUT)
            .build()

        return RestTemplate(
            HttpComponentsClientHttpRequestFactory().apply {
                httpClient = HttpClientBuilder
                    .create()
                    .setDefaultRequestConfig(config)
                    .build()
            }
        )
    }

    /**
     * Rest template for ocp api access
     *
     * @return RestTemplate
     */
    @Bean
    open fun ocpRestTemplate(): RestTemplate {
        val config = RequestConfig.custom()
            .setConnectTimeout(AppConstants.OCP_CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(AppConstants.OCP_CONNECT_REQ_TIMEOUT)
            .setSocketTimeout(AppConstants.OCP_SOCKET_TIMEOUT)
            .build()

        return RestTemplate(
            HttpComponentsClientHttpRequestFactory().apply {
                httpClient = HttpClientBuilder
                    .create()
                    .setDefaultRequestConfig(config)
                    .build()
            }
        )
    }
}
