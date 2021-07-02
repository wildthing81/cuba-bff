/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubmissionDto(
    @JsonProperty("id") var id: UUID?,
    @JsonProperty var borrower: Any?,
    @JsonProperty var types: List<String>?,
    @JsonProperty var purposes: List<String>?,
    @JsonProperty var due: String?,
    @JsonProperty var note: String?,
    @JsonProperty var status: String?,
    @JsonProperty var workflowStep: Int? = null,
    @JsonProperty var workflow: WorkflowDto? = null,
    @JsonProperty var sections: List<SectionDto>? = null,
    @JsonProperty var tasks: List<TaskDto>? = null,
    @JsonProperty var team: MutableSet<Any>? = null,
    @JsonProperty var creator: UserDto? = null,
    @param:JsonProperty(value = "isFlagged")
    @get:JsonProperty("isFlagged") var flagged: Boolean? = null,
    @JsonProperty var isViewed: Boolean? = null,
    @JsonProperty var createdAt: String? = null,
    @JsonProperty var actions: SubmissionActionDto? = null,
    @JsonProperty var workflowStepName: String? = null,
    @JsonProperty var displayRef: String? = null
) : Serializable
