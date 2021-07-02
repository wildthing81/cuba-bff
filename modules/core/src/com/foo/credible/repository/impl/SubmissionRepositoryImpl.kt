/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.SectionDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.SubmissionPurpose
import com.anzi.credible.entity.SubmissionSection
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.Workflow
import com.anzi.credible.exceptions.CrdUserException
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.repository.SubmissionRepository
import com.anzi.credible.repository.UserRepository
import com.anzi.credible.utils.AppUtils.displayRef
import com.anzi.credible.utils.DateUtils.toUTC
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.EntitySet
import com.haulmont.cuba.core.global.View
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

@Repository
open class SubmissionRepositoryImpl : SubmissionRepository {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var dataManager: DataManager

    /**
     * Query Submission by Id from database
     *
     * @param submissionId
     * @param viewName
     * @return
     */
    override fun fetchSubmissionById(submissionId: String, viewName: String): Submission = dataManager
        .load(Submission::class.java)
        .id(UUID.fromString(submissionId))
        .view(viewName).one()

    /**
     * query all user assigned submissions
     *
     * @param loggedUser
     * @param viewName
     * @return assigned submissions
     */
    override fun fetchAssignedSubmissions(loggedUser: User, viewName: String): List<Submission> = try {
        val submissions = dataManager
            .load(Submission::class.java)
            .query(QueryConstants.FETCH_ASSIGNED_SUBMISSIONS)
            .parameter("user", loggedUser)
            // .parameter("status", status)
            .view(viewName).list()

        log.debug { "assigned submissions: $submissions" }
        submissions
    } catch (dbe: Exception) {
        log.error(dbe) { "Error fetching assigned submissions" }
        throw CrdUserException(ErrorConstants.FIND_ENTITY)
    }

    /**
     * query all user watched submissions
     *
     * @param loggedUser
     * @param viewName
     * @return watched submissions
     */
    override fun fetchWatchedSubmissions(loggedUser: User, viewName: String): List<Submission> = dataManager
        .load(Submission::class.java)
        .query(QueryConstants.FETCH_WATCHED_SUBMISSIONS)
        .parameter("user", loggedUser)
        // .parameter("status", status)
        .view(viewName).list()

    /**
     * update existing submission
     *
     * @param commitContext
     */
    override fun updateSubmission(commitContext: CommitContext): EntitySet = dataManager.commit(commitContext)

    /**
     * Persists a new Submission
     *
     * @param submissionDto
     * @param purposes
     * @param sections
     * @param institution
     * @return Submission entity
     */
    override fun createSubmission(
        submissionDto: SubmissionDto,
        purposes: HashMap<String, String>,
        sections: List<SectionDto>,
        institution: Institution,
        workflow: Workflow,
    ): Submission {
        log.debug { "Start: Saving a new Submission" }
        val commitContext = CommitContext()
        val submission = dataManager.create(Submission::class.java)
        commitContext.addInstanceToCommit(submission)

        addBasicDetails(submission, submissionDto)
        addPurposes(submission, purposes, commitContext)
        addSections(submission, sections, commitContext)
        addTeam(submission, submissionDto, commitContext)
        createAssociations(submission, submissionDto.borrower as String, institution, workflow)

        dataManager.commit(commitContext)
        log.debug { "End: Saving a new Submission" }
        return dataManager.reload(submission, ViewConstants.SUBMISSION_FETCH)
    }

    private fun addPurposes(
        submission: Submission,
        purposes: HashMap<String, String>,
        commitContext: CommitContext,
    ) = purposes.forEach { (p, t) ->
        val submissionPurpose = dataManager.create(SubmissionPurpose::class.java)
        submissionPurpose.submission = submission
        submissionPurpose.purpose = p
        submissionPurpose.type = t
        commitContext.addInstanceToCommit(submissionPurpose)
    }

    private fun addBasicDetails(
        submission: Submission,
        submissionDto: SubmissionDto,
    ) {
        // submission.startDate = submissionDto.start!!.toUTC()
        submission.due = submissionDto.due!!.toUTC()
        submission.note = submissionDto.note
        submission.displayRef = displayRef(3, 3)
    }

    private fun addTeam(
        submission: Submission,
        submissionDto: SubmissionDto,
        commitContext: CommitContext,
    ) {
        submission.team = userRepository.getUsersFromIds(submissionDto.team!!.map { id -> id.toString() })
            .toMutableSet()
        submission.team.forEach { commitContext.addInstanceToCommit(it) }
    }

    private fun addSections(
        submission: Submission,
        sections: List<SectionDto>,
        commitContext: CommitContext,
    ) {
        sections.map { it ->
            val section = dataManager.create(SubmissionSection::class.java)
            section.apply {
                slug = it.slug
                content = it.content
                updatedBy = it.updatedBy as String?
                createTs = it.createdAt!!.toUTC()
                updateTs = it.updatedAt!!.toUTC()
                exceptions = it.exceptions.toString()
                this.submission = submission
            }
            commitContext.addInstanceToCommit(section)
        }
    }

    private fun createAssociations(
        submission: Submission,
        borrowerId: String,
        institution: Institution?,
        workflow: Workflow,
    ) {
        submission.borrower = dataManager.load(Borrower::class.java).id(UUID.fromString(borrowerId)).one()
        submission.institution = institution
        submission.workflow = workflow
        submission.setStatus(workflow.getInitialStatus())
        submission.workflowStep = 1
    }

    private fun taskView() = View(Task::class.java)
        .addProperty("submission")
        .addProperty("assignee")
        .addProperty("flaggedUsers")
}
