/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommentDto(
    @JsonProperty("id") var commentId: UUID?,
    @JsonProperty var submissionId: String? = null,
    @JsonProperty var text: String? = null,
    @JsonProperty var createdAt: String? = null,
    @JsonProperty var createdBy: UserDto? = null,
    @JsonProperty var updatedAt: String? = null,
) : Serializable
