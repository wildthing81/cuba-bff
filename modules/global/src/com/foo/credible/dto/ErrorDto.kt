/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ErrorDto(
    @JsonProperty var entity: String,
    @JsonProperty var id: String?,
    @JsonProperty var errorMessage: String,
    @JsonProperty var errorDetails: String?
) : Serializable
