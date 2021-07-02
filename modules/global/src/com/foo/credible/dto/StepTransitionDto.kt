/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StepTransitionDto(
    @JsonProperty var to: Int?,
    @JsonProperty var label: String?,
    @JsonProperty var trigger: String? = null,
    @JsonProperty var submissionStatus: String? = null,
    @JsonProperty var borrowerRefresh: Boolean? = null
) : Serializable
