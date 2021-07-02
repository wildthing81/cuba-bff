/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import com.anzi.credible.entity.Activity
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.repository.ActivityRepository
import com.anzi.credible.repository.QueryConstants
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@Repository
open class ActivityRepositoryImpl : ActivityRepository {
    @Inject
    private lateinit var dataManager: DataManager

    /**
     * query database to fetch activity by id
     *
     * @param activityId
     * @param viewName
     * @return Activity
     */
    override fun fetchActivityById(activityId: String, viewName: String): Activity = dataManager
        .load(Activity::class.java)
        .id(UUID.fromString(activityId)).view(viewName).one()

    /**
     * Fetches all activities for the submission between given trimeframe
     *
     * @param submissionId
     * @param viewName
     * @param startAt
     * @param endAt
     *
     * @return Activities
     */
    override fun fetchActivitiesBySubmissionAndTimeFrame(
        submissionId: String,
        viewName: String,
        startAt: Date,
        endAt: Date
    ): List<Activity>? = dataManager
        .load(Activity::class.java)
        .query(QueryConstants.FETCH_SUBMISSION_ACTIVITY)
        .parameter("submissionEntity", Submission::class.toString())
        .parameter("submissionId", submissionId)
        .parameter("startAt", startAt)
        .parameter("endAt", endAt)
        .view(viewName).list()

    /**
     * Creates new Activity
     *
     * @param institution
     * @param submissionId
     * @param activityDetails
     * @return Activity
     */
    override fun createActivity(institution: Institution, submissionId: String, activityDetails: String): Activity {
        val commitContext = CommitContext()
        val activity = dataManager.create(Activity::class.java)

        commitContext.addInstanceToCommit(activity)
        activity.idKey = Submission::class.toString()
        activity.idValue = submissionId
        activity.details = activityDetails
        activity.institution = institution
        dataManager.commit(commitContext)

        return activity
    }
}
