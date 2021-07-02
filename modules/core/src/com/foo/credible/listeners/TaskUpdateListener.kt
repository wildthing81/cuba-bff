/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Component
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.TaskCategory
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.TaskUpdate
import com.anzi.credible.helpers.TaskHelper
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.TransactionalDataManager
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.global.DataManager
import mu.KotlinLogging

@Component("TaskUpdateListener")
open class TaskUpdateListener : StandardEntityListener<TaskUpdate> {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var dataMgr: DataManager

    @Inject
    private lateinit var txDM: TransactionalDataManager

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var activityService: ActivityService

    override fun beforeEntityCommit(event: EntityChangedEvent<TaskUpdate, UUID>) {
        log.debug { "Before taskupdate commit listener method" }
        try {
            if (event.type == EntityChangedEvent.Type.CREATED) {
                txDM.load(event.entityId).view(ViewConstants.TASKUPDATE_FETCH).one().let { taskUpdate ->
                    taskUpdate.getTaskStatus()?.run {
                        taskUpdate.task!!.apply {
                            setTaskStatus(taskUpdate.getTaskStatus())
                            note = taskUpdate.note
                            txDM.save(this)
                        }
                    }
                }
            }
        } catch (dbe: Exception) {
            log.error(dbe) { "Error updating task status from status of taskupdate: ${event.entityId}" }
            throw dbe
        }
    }

    override fun afterEntityCommit(event: EntityChangedEvent<TaskUpdate, UUID>) {
        log.debug { "Creating Activity for task update: $event.entityId" }
        dataMgr.load(event.entityId).view(ViewConstants.TASKUPDATE_FETCH).one().let {
            when (event.type) {
                EntityChangedEvent.Type.CREATED ->
                    when (it.task!!.getTaskCategory()) {
                        TaskCategory.WORK -> workActivity(it)
                        TaskCategory.DECISION -> decisionActivity(it)
                    }
                EntityChangedEvent.Type.UPDATED -> generateUpdateActivity(it as TaskUpdate)
                else -> { }
            }
        }
    }

    private fun generateUpdateActivity(taskUpdate: TaskUpdate) {
        // @TODO: add activity for update
    }

    private fun workActivity(taskUpdate: TaskUpdate) {
        val payload = ObjectMapper().createObjectNode()
            .put("key", "id")
            .put("value", taskUpdate.id.toString())
        val payloadJson = ObjectMapper().createArrayNode()
            .add(payload)
        val actionJson = ObjectMapper().createObjectNode()
            .put("label", "View Work Task")
            .set<JsonNode>("payload", payloadJson)
        val json = ObjectMapper().createObjectNode()
            .put("type", "work")
            .put("timestamp", taskUpdate.createTs?.formatTo())
            .put(
                "status",
                taskUpdate.task?.getTaskStatus()?.let { status ->
                    TaskHelper.taskUpdateActivityStatus(status).id
                }
            )
            .put(
                "priority",
                taskUpdate.task?.getTaskStatus()?.let { status ->
                    TaskHelper.taskUpdateActivityPriority(status).id
                }
            )
            .put(
                "message",
                AppConstants.ACTIVITY_TASK_UPDATE.format(
                    UserHelper.userFullName(userService.fetchUsersByLogin(listOf(taskUpdate.createdBy))[0]),
                    taskUpdate.status
                )
            )
            .set<JsonNode>("action", actionJson)
        activityService.addActivity(taskUpdate.task?.submission?.id.toString(), json.toPrettyString())
    }

    private fun decisionActivity(taskUpdate: TaskUpdate) {
        val payload = ObjectMapper().createObjectNode()
            .put("key", "id")
            .put("value", taskUpdate.id.toString())
        val payloadJson = ObjectMapper().createArrayNode()
            .add(payload)
        val actionJson = ObjectMapper().createObjectNode()
            .put("label", "View ${taskUpdate.task?.category}")
            .set<JsonNode>("payload", payloadJson)
        val json = ObjectMapper().createObjectNode()
            .put("type", "decision")
            .put("timestamp", taskUpdate.createTs?.formatTo())
            .put(
                "status",
                taskUpdate.task?.getTaskStatus()?.let { status ->
                    TaskHelper.taskUpdateActivityStatus(status).id
                }
            )
            .put(
                "priority",
                taskUpdate.task?.getTaskStatus()?.let { status ->
                    TaskHelper.taskUpdateActivityPriority(status).id
                }
            )
            .put(
                "message",
                AppConstants.ACTIVITY_TASK_UPDATE.format(
                    UserHelper.userFullName(userService.fetchUsersByLogin(listOf(taskUpdate.createdBy))[0]),
                    taskUpdate.status
                )
            )
            .set<JsonNode>("action", actionJson)
        activityService.addActivity(taskUpdate.task?.submission?.id.toString(), json.toPrettyString())
    }
}
