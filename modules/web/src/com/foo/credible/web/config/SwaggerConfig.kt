package com.foo.credible.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableSwagger2
open class SwaggerConfig {

    @Primary
    @Bean
    open fun swaggerResourcesProvider(resourcesProvider: InMemorySwaggerResourcesProvider): SwaggerResourcesProvider? {
        return SwaggerResourcesProvider {
            val wsResource = SwaggerResource().apply {
                name = "credible"
                swaggerVersion = "2.0"
                location = "/swagger2/apis-docs.yml"
            }
            val resources = ArrayList(resourcesProvider.get())
            resources.add(wsResource)
            resources
        }
    }
}
