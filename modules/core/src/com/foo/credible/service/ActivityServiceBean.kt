/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.Date
import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.ErrorDto
import com.foo.credible.helpers.ActivityHelper
import com.anzi.credible.repository.ActivityRepository
import mu.KotlinLogging

@Service(ActivityService.NAME)
class ActivityServiceBean : ActivityService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var activityRepository: ActivityRepository

    @Inject
    private lateinit var userService: UserService

    override fun addActivity(submissionId: String, activityDetails: String) = try {
        log.info { "Adding activity for submission: $submissionId" }
        val institution = userService.fetchLoggedUser().institution
        institution?.let { activityRepository.createActivity(it, submissionId, activityDetails) }
    } catch (dbe: Exception) {
        log.error(dbe) { "Issue in creating activity for: $submissionId" }
        ErrorDto(
            "Activity",
            submissionId,
            ErrorConstants.CREATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch Activity by Id
     *
     * @param submissionId
     * @param startAt
     * @param endAt
     * @return ActivityDto or ErrorDto
     */
    override fun fetchActivities(submissionId: String, startAt: Date, endAt: Date) = try {
        log.info { "Fetching activities for submission: $submissionId" }
        activityRepository.fetchActivitiesBySubmissionAndTimeFrame(
            submissionId,
            ViewConstants.ACTIVITY_FETCH,
            startAt,
            endAt
        )!!.let { activities ->
            activities.map { activity ->
                com.foo.credible.helpers.ActivityHelper.buildDtoForList(activity)
            }
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "No activity exists for: $submissionId" }
        ErrorDto(
            "Activity",
            submissionId,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }
}
