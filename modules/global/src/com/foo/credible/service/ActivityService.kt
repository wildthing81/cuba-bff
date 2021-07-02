/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.Date

interface ActivityService {
    companion object {
        const val NAME = "crd_ActivityService"
    }

    fun addActivity(submissionId: String, activityDetails: String): Any?

    fun fetchActivities(submissionId: String, startAt: Date, endAt: Date): Any?
}
