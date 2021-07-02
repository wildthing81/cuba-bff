/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.constants.SubmissionStatus
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.SubmissionSection
import com.anzi.credible.helpers.BorrowerHelper
import com.anzi.credible.helpers.InstitutionHelper
import com.anzi.credible.helpers.SubmissionHelper
import com.anzi.credible.helpers.SubmissionSectionsHelper
import com.anzi.credible.helpers.TaskHelper
import com.anzi.credible.helpers.WorkflowHelper
import com.anzi.credible.repository.BorrowerRepository
import com.anzi.credible.repository.SubmissionRepository
import com.anzi.credible.repository.TaskRepository
import com.anzi.credible.repository.WorkflowRepository
import com.anzi.credible.utils.AppUtils.createIfNull
import com.anzi.credible.utils.AppUtils.then
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.global.CommitContext
import mu.KotlinLogging
import java.util.Date
import com.anzi.credible.constants.NotificationConstants.SUBMISSION_CREATED_TEXT
import com.anzi.credible.constants.NotificationConstants.SUBMISSION_TEAM_UPDATED_TEXT
import com.anzi.credible.constants.NotificationType
import com.anzi.credible.dto.KeyValueDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.helpers.UserHelper

@Service(SubmissionService.NAME)
open class SubmissionServiceBean : SubmissionService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var submissionRepository: SubmissionRepository

    @Inject
    private lateinit var borrowerRepository: BorrowerRepository

    @Inject
    private lateinit var workflowRepository: WorkflowRepository

    @Inject
    private lateinit var taskRepository: TaskRepository

    @Inject
    private lateinit var notificationService: NotificationService

    /**
     * Fetch all 'in-progress' submissions for logged user or submissions count if path variable is
     * non-null
     *
     * @return list of submissions
     */
    override fun fetchAssignedSubmissions(pathVar: String?) = try {
        val loggedUser = userService.fetchLoggedUser()
        submissionRepository.fetchAssignedSubmissions(loggedUser).let { submissions ->
            if (pathVar.isNullOrEmpty()) submissions.sortedByDescending { it.updateTs }
                .map { submission ->
                    SubmissionHelper
                        .buildDtoForList(submission, loggedUser)
                }
            else getSubmissionCount(submissions, loggedUser)
        }
    } catch (dbe: Exception) {
        log.error { "Error fetching assigned submissions" }
        ErrorDto(
            Submission::class.simpleName!!,
            "",
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch submission by id
     *
     * @param submissionId
     * @return
     */
    override fun fetchSubmission(submissionId: String) = try {
        submissionRepository.fetchSubmissionById(submissionId)?.let { submission ->
            val loggedUser = userService.fetchLoggedUser()
            submission.viewedUsers?.none { loggedUser == it }?.then {
                updateSubmissionViewStatusOnFetch(submission, loggedUser)
            }
            SubmissionHelper.buildDto(
                submission,
                buildSectionDto(submission.sections),
                loggedUser,
                userService.fetchUsersByLogin(listOf(submission.createdBy))[0]
            )
        }
    } catch (dbe: Exception) {
        log.error { "No submissions exists for: $submissionId" }
        ErrorDto(
            Submission::class.simpleName!!,
            submissionId,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch all watched 'in-progress' submissions for logged user or count if path variable is
     * non-null
     *
     * @return list of submissions
     */
    override fun fetchWatchedSubmissions(pathVar: String?) = try {
        val loggedUser = userService.fetchLoggedUser()
        submissionRepository.fetchWatchedSubmissions(loggedUser).let {
            if (pathVar.isNullOrEmpty()) it.map { submission ->
                SubmissionHelper.buildDtoForList(
                    submission,
                    loggedUser
                )
            }
            else it.size
        }
    } catch (dbe: Exception) {
        log.error { "Error fetching watched submissions" }
        ErrorDto(
            Submission::class.simpleName!!,
            "",
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Transit from current workflow step
     *
     * @param submissionId
     * @param toStepIndex
     * @return
     */
    override fun actionStepTransition(submissionId: String, toStepIndex: Int, trigger: StepTrigger) = try {
        log.info { "Performing workflow step transition to $toStepIndex for submission :$submissionId" }
        submissionRepository.fetchSubmissionById(submissionId, ViewConstants.SUBMISSION_WORKFLOW)?.let { submission ->
            WorkflowHelper.isValidTransition(submission, toStepIndex, trigger)?.run {
                submission.workflowStep = toStepIndex
                submission.setStatus(this.getStatus())
                (submission.getStatus() == SubmissionStatus.APPROVED).then {
                    updateTasksOnTransition(submission)
                }
                submissionRepository.updateSubmission(CommitContext(submission))
                this.borrowerRefresh!!.then {
                    borrowerRepository.updateBorrower(BorrowerHelper.refreshBorrowerDefaults(submission))
                }
                true
            } ?: false
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "${ErrorConstants.ERR_STEP_TRANSITION}: $submissionId" }
        ErrorDto(
            Submission::class.simpleName!!,
            submissionId,
            ErrorConstants.ERR_STEP_TRANSITION,
            dbe.message
        )
    }

    override fun flaggedForUser(submissionId: String, flag: Boolean) = try {
        submissionRepository.fetchSubmissionById(submissionId)?.let {
            val loggedUser = userService.fetchLoggedUser()
            if (flag) it.flaggedUsers.add(loggedUser) else it.flaggedUsers.remove(loggedUser)
            submissionRepository.updateSubmission(CommitContext(it))
        }
    } catch (dbe: Exception) {
        log.error { "Error flagging submission: $submissionId for user" }
        ErrorDto(
            Submission::class.simpleName!!,
            submissionId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Update submission  - sections, team members, types, purpose etc
     *
     * @param submissionId
     * @param submissionDto
     */
    override fun updateSubmission(submissionId: String, submissionDto: SubmissionDto) = try {
        submissionRepository.fetchSubmissionById(submissionId)?.let { submission ->
            val commitContext = CommitContext()
            val loggedUser = userService.fetchLoggedUser()
            submissionDto.sections?.let {
                val deletedSections = SubmissionHelper.updateSections(submission.sections, submissionDto)
                commitContext.setRemoveInstances(deletedSections as List<SubmissionSection>)
                commitContext.setCommitInstances(submission.sections as List<SubmissionSection>)
            }
            submissionDto.team?.let {
                submission.team.addAll(userService.fetchUsersById(it.toList() as List<String>))
                notificationService.createNotification(
                    NotificationDto(
                        SUBMISSION_TEAM_UPDATED_TEXT.format(
                            UserHelper.userFullName(loggedUser),
                            submission.borrower?.name
                        ),
                        listOf(
                            KeyValueDto("submissionId", submissionId),
                            KeyValueDto("userId", loggedUser.id.toString())
                        ),
                        NotificationType.SUBMISSION_TEAM_UPDATED.id
                    ),
                    it.toList() as List<String>
                )
            }
            commitContext.addInstanceToCommit(submission)
        }?.also {
            submissionRepository.updateSubmission(it)
        }
    } catch (dbe: Exception) {
        log.error { "Error updating submission: $submissionId" }
        ErrorDto(
            Submission::class.simpleName!!,
            submissionId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Creates a new submission & related entities
     *
     * @param submissionDto
     * @return
     * @throws
     */
    override fun createSubmission(submissionDto: SubmissionDto) = try {
        log.info { "Creating a new submission for borrower: ${submissionDto.borrower}" }
        val mapper = ObjectMapper()
        val loggedUser = userService.fetchLoggedUser()
        val institution = loggedUser.institution!!
        val borrower = borrowerRepository.fetchBorrowerById(submissionDto.borrower!! as String)
        val sections = SubmissionSectionsHelper.createDefaultSections(
            InstitutionHelper.getTemplates(institution),
            InstitutionHelper.getExceptions(institution),
            mapper.readTree(borrower.submissionDefaults),
            loggedUser
        )

        val purposes = SubmissionHelper.createSubmissionPurposes(
            InstitutionHelper.getTypes(institution),
            submissionDto
        )

        workflowRepository.fetchWorkFlowBySubmissionType(submissionDto.types!![0], ViewConstants.WORKFLOW_FETCH)!!
            .let { workflow ->
                submissionRepository.createSubmission(
                    submissionDto.apply { team = team.createIfNull(loggedUser.id.toString()) },
                    purposes,
                    sections,
                    institution,
                    workflow
                )
            }?.let { submission ->
                log.info { "New submission ${submission.id}" }
                (submissionDto.team as MutableSet<String>).filter { appUser -> appUser != loggedUser.id.toString() }
                    .let {
                        notificationService.createNotification(
                            NotificationDto(
                                SUBMISSION_CREATED_TEXT.format(UserHelper.userFullName(loggedUser), borrower.name),
                                listOf(KeyValueDto("submissionId", submission.id.toString())),
                                NotificationType.SUBMISSION_CREATED.id
                            ),
                            it
                        )
                    }
                SubmissionHelper.buildDto(
                    submission,
                    buildSectionDto(submission.sections),
                    loggedUser,
                    userService.fetchUsersByLogin(listOf(submission.createdBy))[0]
                )
            }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new submission for borrower: ${submissionDto.borrower}" }
        ErrorDto(Submission::class.simpleName!!, null, ErrorConstants.CREATE_ENTITY, dbe.message)
    }

    private fun updateTasksOnTransition(submission: Submission) = submission.tasks.map { task ->
        when (task.getTaskStatus()) {
            TaskStatus.PENDING, TaskStatus.REWORK -> {
                TaskHelper.createTaskUpdateEntities(task, TaskStatus.COMPLETE, "Auto Update to complete").run {
                    val entitySet = taskRepository.createTaskUpdates(this)
                    log.info { "Task updated ${task.id}" }
                    log.info { "Total updates: ${entitySet?.size}" }
                }
            }
            else -> {
            } // Nothing
        }
    }

    private fun buildSectionDto(sections: MutableList<SubmissionSection>) = sections.map { section ->
        SubmissionSectionsHelper.buildDto(section, userService.fetchUsersByLogin(listOf(section.updatedBy))[0])
    }

    private fun updateSubmissionViewStatusOnFetch(submission: Submission, loggedUser: AppUser) {
        submission.viewedUsers?.add(loggedUser)
        submissionRepository.updateSubmission(CommitContext(submission))
    }

    /**
     * Returns count of submissions that are either not viewed by user or overdue
     *
     * @param submissions
     * @param loggedUser
     * @return
     */
    private fun getSubmissionCount(submissions: List<Submission>, loggedUser: AppUser): Int {
        var count = 0
        submissions.forEach { submission ->
            if (submission.viewedUsers?.none { user -> loggedUser == user }!! or submission.due?.before(Date())!!) {
                count++
            }
        }
        return count
    }
}
