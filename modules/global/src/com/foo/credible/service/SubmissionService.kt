/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.dto.SubmissionDto

interface SubmissionService {
    companion object {
        const val NAME = "crd_SubmissionService"
    }

    fun createSubmission(submissionDto: SubmissionDto): Any?

    fun fetchSubmission(submissionId: String): Any?

    fun fetchAssignedSubmissions(pathVar: String?): Any

    fun fetchWatchedSubmissions(pathVar: String?): Any

    fun actionStepTransition(
        submissionId: String,
        toStepIndex: Int,
        trigger: StepTrigger = StepTrigger.USER_INITIATED
    ): Any?

    fun flaggedForUser(submissionId: String, flag: Boolean): Any?

    fun updateSubmission(submissionId: String, submissionDto: SubmissionDto): Any?

    // fun deleteSubmission(submissionId: String): Any
}
