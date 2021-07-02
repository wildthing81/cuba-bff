/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FluxActionDto(
    @JsonProperty var type: String? = null,
    @JsonProperty var payload: List<KeyValueDto>,
) : Serializable
