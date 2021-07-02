/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SectionDto(
    @JsonProperty var slug: String?,
    @JsonProperty var content: String?,
    @JsonProperty var updatedBy: Any? = null,
    @JsonProperty var exceptions: JsonNode? = null,
    @JsonProperty var createdAt: String?,
    @JsonProperty var updatedAt: String?,
    @JsonProperty var comments: List<String>? = null
) : Serializable
