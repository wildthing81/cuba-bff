/*
 * The code is copyright ©2021
 */

package com.foo.credible.config

import com.anzi.credible.constants.AppConstants.OCP_REST_ENABLED
import com.haulmont.cuba.core.config.Config
import com.haulmont.cuba.core.config.Property
import com.haulmont.cuba.core.config.Source
import com.haulmont.cuba.core.config.SourceType
import com.haulmont.cuba.core.global.Secret

@Source(type = SourceType.DATABASE)
interface AppConfig : Config {
    @get:Property("credible.ckeditor.environmentId")
    val ckEditorEnvironmentId: String

    @get:Secret
    @get:Property("credible.ckeditor.secret")
    val ckEditorSecret: String

    @get:Source(type = SourceType.APP)
    @get:Property("credible.profile")
    val profile: String

    @get:Source(type = SourceType.APP)
    @get:Property("credible.ws.allowedOrigins")
    val wsOrigins: String

    @get:Source(type = SourceType.APP)
    @get:Property("ocp.rest.baseurl")
    val ocpRestBaseUrl: String

    @get:Property(OCP_REST_ENABLED)
    val ocpRestEnabled: String
}
