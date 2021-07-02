/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Component
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.Submission
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.global.DataManager
import mu.KotlinLogging

@Component("SubmissionListener")
open class SubmissionListener : StandardEntityListener<Submission> {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var txDM: DataManager

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var activityService: ActivityService

    override fun beforeEntityCommit(event: EntityChangedEvent<Submission, UUID>) {
        log.debug { "BeforeEntityCommit for submission" }
    }

    override fun afterEntityCommit(event: EntityChangedEvent<Submission, UUID>) {
        log.debug { "Creating activity for submission: $event.entityId" }
        try {
            txDM.load(event.entityId).view(ViewConstants.SUBMISSION_FETCH).one().let {
                when (event.type) {
                    EntityChangedEvent.Type.CREATED -> generateCreateActivity(it as Submission)
                    EntityChangedEvent.Type.UPDATED -> generateUpdateActivity(it as Submission)
                    else -> { }
                }
            }
        } catch (dbe: Exception) {
            log.error(dbe) { "Error creating activity for submission: ${event.entityId}" }
            throw dbe
        }
    }

    private fun generateCreateActivity(submission: Submission) {
        val createdByUser = userService.fetchUsersByLogin(listOf(submission.createdBy))[0]
        val json = ObjectMapper().createObjectNode()
            .put("type", "submission")
            .put("timestamp", submission.createTs?.formatTo())
            .put(
                "message",
                AppConstants.ACTIVITY_SUBMISSION_CREATE.format(
                    UserHelper.userFullName(createdByUser)
                )
            )
        activityService.addActivity(submission.id.toString(), json.toPrettyString())
    }

    private fun generateUpdateActivity(submission: Submission) {
        val updatedByUser = userService.fetchUsersByLogin(listOf(submission.updatedBy))[0]
        val json = ObjectMapper().createObjectNode()
            .put("type", "submission")
            .put("timestamp", submission.updateTs?.formatTo())
            .put(
                "message",
                AppConstants.ACTIVITY_SUBMISSION_UPDATE.format(
                    UserHelper.userFullName(updatedByUser)
                )
            )
        activityService.addActivity(submission.id.toString(), json.toPrettyString())
    }
}
