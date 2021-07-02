/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

object QueryConstants {
    // Institution
    const val FETCH_BY_SITEID = "select i from Institution i where i.siteId = :siteId"

    // Submission
    const val FETCH_ASSIGNED_SUBMISSIONS = "select s from Submission s where :user member of s" +
        ".team"
    const val FETCH_WATCHED_SUBMISSIONS = "select s from Submission s join s.borrower b where :user member of b" +
        ".watchers"

    // Task
    const val FETCH_ASSIGNED_TASKS = "select t from Task t where t.assignee = :user"
    const val FETCH_CREATED_TASKS = "select t from Task t where t.createdBy = :user"
    const val FETCH_BY_BORROWER_NAME = "select b from Borrower b where b.name = :name"

    // User
    const val FETCH_USERS_BY_LOGIN = "select u from AppUser u where u.login in :logins"
    const val FETCH_USERS_BY_ID = "select u from AppUser u where u.id in :ids"
    const val FETCH_GROUP_BY_NAME = "select g from sec\$Group g where g.name = :groupName"
    const val FETCH_INSTITUTION_USERS = "select u from AppUser u where u.institution = :institution"

    // Activity
    const val FETCH_SUBMISSION_ACTIVITY =
        "select a from Activity a where a.idKey = :submissionEntity and a.idValue= :submissionId " +
            "and a.createTs between :startAt and :endAt"

    // Workflow
    const val FETCH_WORKFLOW_BY_SUB_TYPE = "select w from Workflow w where (0 < LOCATE(:subType, w.submissionTypes))"
    const val FETCH_INSTITUTION_WORKFLOWS = "select w from Workflow w where w.institution = :institution"

    // Comments
    const val FETCH_SUBMISSION_COMMENTS = "select c from Comment c where c.submission= :submission"

    // Notification
    const val FETCH_USER_NOTIFICATIONS = "select s.notification from Subscriber s where s.user = :user and " +
        "s.notification.createTs BETWEEN :startAt AND :endAt"
    const val MARK_ALL_USER_NOTIFICATIONS_READ = "update Subscriber s set s.isRead = true where s.user = :user and " +
        "s.isRead = false"
}
