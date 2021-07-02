/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WorkflowDto(
    @JsonProperty var id: UUID?,
    @JsonProperty var name: String?,
    @JsonProperty var steps: List<Any>? = null,
    @JsonProperty var transitions: List<StepTransitionDto>? = null,
    @JsonProperty var current: Int? = null,
    @JsonProperty var initialStatus: String? = null,
    @JsonProperty var submissionTypes: List<String>? = null,
    @JsonProperty var author: UserDto? = null,
    @JsonProperty var createdAt: String? = null,
    @JsonProperty var updatedAt: String? = null,
    @JsonProperty var version: Int? = null
) : Serializable
