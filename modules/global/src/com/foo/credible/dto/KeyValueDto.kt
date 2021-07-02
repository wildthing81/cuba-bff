/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class KeyValueDto(
    @JsonProperty var key: String,
    @JsonProperty var value: String
) : Serializable
