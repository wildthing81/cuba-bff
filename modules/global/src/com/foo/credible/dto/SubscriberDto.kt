package com.foo.credible.dto

import java.io.Serializable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscriberDto(
    @JsonProperty var isRead: Boolean = false,
    @JsonProperty var isHidden: Boolean = false,
    @JsonProperty var notification: Any? = null,
    @JsonProperty var user: UserDto? = null,
) : Serializable
