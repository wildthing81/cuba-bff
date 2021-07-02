/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WorkflowStepDto(
    @JsonProperty var index: Int?,
    @JsonProperty var name: String?,
    @JsonProperty var layout: JsonNode? = null,
    @JsonProperty var transitions: List<StepTransitionDto>? = null,
) : Serializable
