/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.SectionDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Workflow
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.EntitySet
import com.haulmont.cuba.security.entity.User

interface SubmissionRepository {

    fun fetchSubmissionById(submissionId: String, viewName: String = ViewConstants.SUBMISSION_FETCH): Submission?

    fun createSubmission(
        submissionDto: SubmissionDto,
        purposes: HashMap<String, String>,
        sections: List<SectionDto>,
        institution: Institution,
        workflow: Workflow
    ): Submission?

    fun fetchAssignedSubmissions(loggedUser: User, viewName: String = ViewConstants.SUBMISSION_LIST): List<Submission>

    fun fetchWatchedSubmissions(loggedUser: User, viewName: String = ViewConstants.SUBMISSION_LIST): List<Submission>

    fun updateSubmission(commitContext: CommitContext): EntitySet?
}
