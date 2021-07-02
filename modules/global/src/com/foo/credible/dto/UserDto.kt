/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import java.io.Serializable
import java.util.UUID
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    @JsonProperty var id: UUID?,
    @JsonProperty var login: String? = null,
    @JsonProperty var name: String? = null,
    @JsonProperty var firstName: String? = null,
    @JsonProperty var middleName: String? = null,
    @JsonProperty var lastName: String? = null,
    @JsonProperty var position: String? = null,
    @JsonProperty var email: String? = null,
    @JsonProperty var profileImage: String? = null,
    @JsonProperty var roles: List<String>? = null,
    @JsonProperty var scope: String? = null,
    @JsonProperty var cadLevel: Int? = null,
    @JsonProperty var preferences: JsonNode? = null,
    @JsonProperty var password: String? = null,
    @JsonProperty var lastNotifiedAt: String? = null
) : Serializable
