/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

/**
 *@Author Ramaswamy R
 *@create 18/1/21 12:09 pm
 */
object AppConstants {
    const val ENCODING_UTF_8 = "utf-8"
    const val COUNT = "count"
    const val ANONYMOUS = "anonymous"
    const val ADMIN = "admin"
    const val SUCCESS = "success"
    const val DEFAULT_GRP_NAME = "Company"

    const val ACTIVITY_SUBMISSION_CREATE = "%s created this submission"
    const val ACTIVITY_SUBMISSION_UPDATE = "%s updated this submission"

    const val ACTIVITY_TASK_CREATE = "%s assigned a %s to %s {action}"
    const val ACTIVITY_TASK_UPDATE = "%s changed the status to %s {action}"

    const val ACTIVITY_COMMENT_REPLY = "%s replied to %s's comment in the $%s section {%d comments}"

    const val ACTION_OVERDUE = "%s overdue"
    const val ACTION_PENDING = "%s pending"

    const val ES_CONNECT_TIMEOUT = 10000
    const val ES_CONNECT_REQ_TIMEOUT = 10000
    const val ES_SOCKET_TIMEOUT = 30000

    const val PROFILE_DEV = "dev"
    const val PROFILE_STAG = "stag"

    const val USER_SESSION = "UserSession"

    const val MARK_ALL_READ = "markAllRead"

    const val OCP_CONNECT_TIMEOUT = 10000
    const val OCP_CONNECT_REQ_TIMEOUT = 10000
    const val OCP_SOCKET_TIMEOUT = 30000
    const val OCP_TOKEN_API = "/api/validatetoken"
    const val OCP_REST_ENABLED = "credible.ocp.rest.enabled"
}
