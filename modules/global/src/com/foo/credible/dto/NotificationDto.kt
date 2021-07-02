package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NotificationDto(
    @JsonProperty var text: String? = null,
    @JsonProperty var payload: List<KeyValueDto>? = null,
    @JsonProperty var type: String? = null,
    @JsonProperty var isRead: Boolean? = null,
    @JsonProperty var isHidden: Boolean? = null,
    @JsonProperty("id") var id: UUID? = null,
    @JsonProperty var createdAt: String? = null,
    @JsonProperty var to: UUID? = null
) : Serializable
