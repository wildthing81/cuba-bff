/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.entity.Activity
import com.anzi.credible.entity.Institution
import java.util.Date

interface ActivityRepository {

    fun fetchActivityById(activityId: String, viewName: String): Activity?

    fun fetchActivitiesBySubmissionAndTimeFrame(
        submissionId: String,
        viewName: String,
        startAt: Date,
        endAt: Date
    ): List<Activity>?

    fun createActivity(institution: Institution, submissionId: String, activityDetails: String): Activity
}
