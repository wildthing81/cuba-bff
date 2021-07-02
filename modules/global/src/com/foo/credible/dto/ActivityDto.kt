/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ActivityDto(
    @JsonProperty var type: String,
    @JsonProperty var message: String,
    @JsonProperty var timestamp: String,
    @JsonProperty var action: ActionDto? = null,
    @JsonProperty var priority: String? = null,
    @JsonProperty var status: String? = null,
) : Serializable
