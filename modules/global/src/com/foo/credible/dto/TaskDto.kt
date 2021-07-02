/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaskDto(
    @JsonProperty("id") var taskId: UUID?,
    @JsonProperty var submissionId: String?,
    @JsonProperty var category: String?,
    @JsonProperty var type: String?,
    @JsonProperty var due: String?,
    @JsonProperty var description: String?,
    @JsonProperty var status: String?,
    @JsonProperty var createdAt: String?,
    @JsonProperty var updatedAt: String?,
    @JsonProperty var creator: UserDto?,
    @JsonProperty var assignee: Any,
    @param:JsonProperty(value = "isFlagged")
    @get:JsonProperty("isFlagged") var flagged: Boolean?,
    @JsonProperty var borrower: Any? = null,
    @JsonProperty var note: String? = null,
    @JsonProperty var isViewed: Boolean? = null
) : Serializable
