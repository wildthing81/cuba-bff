/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Component
import com.anzi.credible.constants.ActivityPriority
import com.anzi.credible.constants.ActivityStatus
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.TaskCategory
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.Task
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.global.DataManager
import mu.KotlinLogging

@Component("TaskListener")
open class TaskListener : StandardEntityListener<Task> {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var txDM: DataManager

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var activityService: ActivityService

    override fun beforeEntityCommit(event: EntityChangedEvent<Task, UUID>) {
        log.debug { "BeforeEntityCommit for task" }
    }

    override fun afterEntityCommit(event: EntityChangedEvent<Task, UUID>) {
        log.debug { "Creating activity for task: $event.entityId" }
        try {
            txDM.load(event.entityId).view(ViewConstants.TASK_FETCH).one().let { task ->
                when (event.type) {
                    EntityChangedEvent.Type.CREATED ->
                        when (task.getTaskCategory()) {
                            TaskCategory.WORK -> workActivity(task)
                            TaskCategory.DECISION -> decisionActivity(task)
                        }
                    else -> {
                    }
                }
            }
        } catch (dbe: Exception) {
            log.error(dbe) { "Error creating activity for Task : ${event.entityId} for ${event.type} " }
            throw dbe
        }
    }

    private fun workActivity(task: Task) {
        val payload = ObjectMapper().createObjectNode()
            .put("key", "id")
            .put("value", task.id.toString())
        val payloadJson = ObjectMapper().createArrayNode()
            .add(payload)
        val actionJson = ObjectMapper().createObjectNode()
            .put("label", "View Work Task")
            .set<JsonNode>("payload", payloadJson)

        /**
         val metaJsonArray = ObjectMapper().createArrayNode()
         .add(
         ObjectMapper().createObjectNode()
         .put("key", "taskId")
         .put("value", task.id.toString())
         )
         .add(
         ObjectMapper().createObjectNode()
         .put("key", "due")
         .put("value", task.due?.formatTo())
         )
         .add(
         ObjectMapper().createObjectNode()
         .put("key", "status")
         .put("value", task.status)
         )
         .add(
         ObjectMapper().createObjectNode()
         .put("key", "assigneeName")
         .put("value", UserHelper.userFullName (userService.fetchUsersByLogin(
         listOf(task.assignee?.login) as List<String>)[0]) as String)
         )
         .add(
         ObjectMapper().createObjectNode()
         .put("key", "assigneeId")
         .put("value", task.assignee?.id.toString())
         )
         **/
        val json = ObjectMapper().createObjectNode()
            .put("type", task.category)
            .put("status", ActivityStatus.CREATED.id)
            .put("priority", ActivityPriority.SUCCESS.id)
            .put(
                "message",
                AppConstants.ACTIVITY_TASK_CREATE.format(
                    UserHelper.userFullName(userService.fetchUsersByLogin(listOf(task.createdBy))[0]),
                    "work task",
                    UserHelper.userFullName(
                        userService.fetchUsersByLogin(
                            listOf(task.assignee?.login) as List<String>
                        )[0]
                    )
                )
            )
            .put("timestamp", task.createTs?.formatTo())
            //   .set<JsonNode>("meta", metaJsonArray)
            .set<JsonNode>("action", actionJson)
        activityService.addActivity(task.submission?.id.toString(), json.toPrettyString())
    }

    private fun decisionActivity(task: Task) {
        val payload = ObjectMapper().createObjectNode()
            .put("key", "id")
            .put("value", task.id.toString())
        val payloadJson = ObjectMapper().createArrayNode()
            .add(payload)
        val actionJson = ObjectMapper().createObjectNode()
            .put("label", "View ${task.type!!.capitalize()} ${task.category!!.capitalize()}")
            .set<JsonNode>("payload", payloadJson)
        val json = ObjectMapper().createObjectNode()
            .put("type", task.category)
            .put(
                "message",
                AppConstants.ACTIVITY_TASK_CREATE.format(
                    UserHelper.userFullName(userService.fetchUsersByLogin(listOf(task.createdBy))[0]),
                    task.type + " " + task.category,
                    UserHelper.userFullName(
                        userService.fetchUsersByLogin(
                            listOf(task.assignee?.login) as List<String>
                        )[0]
                    )
                )
            )
            .put("timestamp", task.createTs?.formatTo())
            .set<JsonNode>("action", actionJson)

        activityService.addActivity(task.submission?.id.toString(), json.toPrettyString())
    }
}
