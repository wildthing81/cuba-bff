/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActionDto(
    @JsonProperty var label: String,
    @JsonProperty var payload: List<KeyValueDto>
) : Serializable
