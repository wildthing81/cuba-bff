/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import java.time.LocalDateTime
import com.anzi.credible.dto.SectionDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.SubmissionSection
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

object SubmissionSectionsHelper {
    private val log = KotlinLogging.logger { }

    fun createDefaultSections(
        template: JsonNode?,
        exceptions: JsonNode?,
        submissionDefaults: JsonNode,
        loggedUser: User
    ) = template!!.mapNotNull { tmpl ->
        submissionDefaults.find { tmpl["slug"] == it["slug"] }?.let { def ->
            val section = SectionDto(
                tmpl["slug"].asText(),
                def.get("text")?.asText(),
                loggedUser.login,
                exceptions,
                LocalDateTime.now().formatTo(),
                LocalDateTime.now().formatTo()
            )

            log.debug { "default section for slug $tmpl['slug']: $section" }
            section
        }
    }

    fun buildDto(section: SubmissionSection, updatedBy: User? = null) =
        SectionDto(
            section.slug,
            section.content,
            updatedBy?.run { UserDto(this.id, name = this.name) },
            ObjectMapper().readTree(section.exceptions),
            section.createTs.formatTo(),
            section.updateTs.formatTo()
        )
}
