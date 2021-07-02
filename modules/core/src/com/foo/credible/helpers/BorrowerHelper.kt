/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Submission
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging

object BorrowerHelper {
    private val log = KotlinLogging.logger { }

    fun refreshBorrowerDefaults(submission: Submission): Borrower {
        log.info { "Refreshing Borrower defaults from submission :${submission.id}" }
        val templateDefaults = ObjectMapper().createArrayNode()
        submission.sections.mapNotNull { section ->
            val templateDefault = ObjectMapper().createObjectNode()
            templateDefault.put("slug", section.slug)
            templateDefault.put("text", section.content)
            templateDefaults.add(templateDefault)
        }
        submission.borrower!!.submissionDefaults = templateDefaults.toString()
        return submission.borrower!!
    }

    fun buildDto(borrower: Borrower) = BorrowerDto(
        borrower.id,
        borrower.name,
        ObjectMapper().readTree(borrower.submissionDefaults),
        borrower.watchers?.map { user -> user.name }?.toMutableSet(),
        borrower.customerType,
        borrower.marketCap,
        borrower.cadLevel,
        borrower.customerGroup,
        borrower.businessUnit,
        borrower.anzsic,
        borrower.ccrRiskScore,
        borrower.securityIndex,
        borrower.externalRatingAndOutLook,
        borrower.lastFullReviewAt!!.formatTo(),
        borrower.lastScheduleReviewAt!!.formatTo(),
        borrower.nextScheduleReviewAt!!.formatTo(),
        borrower.riskSignOff,
        borrower.regulatoryRequirements,
        borrower.team.map { user ->
            UserDto(
                id = user.id,
                name = user.name,
                position = user.position,
                cadLevel = user.cadLevel
            )
        }.toMutableSet()
    )

    fun buildMinimalDto(borrower: Borrower) = BorrowerDto(
        borrower.id,
        borrower.name
    )
}
