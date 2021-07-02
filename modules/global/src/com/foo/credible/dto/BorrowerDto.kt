/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import java.io.Serializable
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BorrowerDto(
    @JsonProperty var id: UUID?,
    @JsonProperty var name: String?,
    @JsonProperty var submissionDefaults: JsonNode? = null,
    @JsonProperty var watchers: MutableSet<String>? = null,
    @JsonProperty var customerType: String? = null,
    @JsonProperty var marketCap: Long? = null,
    @JsonProperty var cadLevel: Int? = null,
    @JsonProperty var customerGroup: String? = null,
    @JsonProperty var businessUnit: String? = null,
    @JsonProperty var anzsic: String? = null,
    @JsonProperty var ccrRiskScore: Int? = null,
    @JsonProperty var securityIndex: String? = null,
    @JsonProperty var externalRatingAndOutLook: Int? = null,
    @JsonProperty var lastFullReviewAt: String? = null,
    @JsonProperty var lastScheduleReviewAt: String? = null,
    @JsonProperty var nextScheduleReviewAt: String? = null,
    @JsonProperty var riskSignOff: String? = null,
    @JsonProperty var regulatoryRequirements: String? = null,
    @JsonProperty var team: MutableSet<Any>? = null,
    @JsonProperty var submissionCount: Int? = null
) : Serializable
