/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.entity.Institution
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

object InstitutionHelper {

    private const val TEMPLATE_KEY = "submissionTemplate"
    private const val TYPES_KEY = "submissionTypes"
    private const val EXCEPTIONS_KEY = "exceptions"
    private const val BORROWER_DEFAULTS_KEY = "borrowerDefaults"

    private val mapper = ObjectMapper()

    fun getTemplates(institution: Institution): JsonNode = mapper.readTree(institution.configuration).get(TEMPLATE_KEY)

    fun getTypes(institution: Institution): JsonNode = mapper.readTree(institution.configuration).get(TYPES_KEY)

    fun getExceptions(institution: Institution): JsonNode =
        mapper.readTree(institution.configuration).get(EXCEPTIONS_KEY)

    fun getBorrowerDefaults(institution: Institution): JsonNode =
        mapper.readTree(institution.configuration).get(BORROWER_DEFAULTS_KEY)
}
