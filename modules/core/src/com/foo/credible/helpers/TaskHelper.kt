/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.constants.ActivityPriority
import com.anzi.credible.constants.ActivityStatus
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.TaskType
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.TaskUpdate
import com.anzi.credible.utils.DateUtils.formatTo
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

private const val OUTDATED_NOTE = "Outdated due to rework required"
private const val WITHDRAWN_NOTE = "Withdrawn due to rework required"

object TaskHelper {
    private val log = KotlinLogging.logger { }

    /**
     * Build a DTO  from Entity
     *
     * @param task
     * @return
     */
    fun buildDto(task: Task, loggedUser: User?, creator: User? = null) =
        TaskDto(
            task.id,
            task.submission!!.id.toString(),
            task.category,
            task.type,
            task.due!!.formatTo(),
            task.description,
            task.status,
            task.createTs.formatTo(),
            task.updateTs.formatTo(),
            creator?.run { UserDto(this.id, name = this.name) },
            task.assignee!!.run { UserDto(this.id, name = this.name) },
            task.flaggedUsers?.any { loggedUser == it },
            task.submission!!.borrower?.run {
                BorrowerDto(id, name)
            },
            task.note,
            task.viewedUsers?.any { user -> loggedUser == user }
        )

    /**
     * Create Task-Update entities
     *
     * @param currentTask
     * @return Any
     */
    fun createTaskUpdateEntities(currentTask: Task, newStatus: TaskStatus?, note: String?): MutableList<TaskUpdate> {
        log.debug("Creating Task Update entities")
        val updateEntity = TaskUpdate()
        updateEntity.note = note
        updateEntity.setTaskStatus(newStatus)
        updateEntity.task = currentTask

        return mutableListOf(updateEntity).also { list ->
            if (newStatus == null) {
                return list
            }

            currentTask.submission!!.tasks.forEach {
                if (it == currentTask) {
                    return@forEach
                }

                if (currentTask.getTaskType() == TaskType.FORMAL && newStatus == TaskStatus.REWORK) {
                    updatePeersOnFormalDeclineOrRework(it)?.apply { list.add(this) }
                }
                if (currentTask.getTaskType() == TaskType.INFORMAL && newStatus == TaskStatus.REWORK) {
                    updatePeersOnInformalRework(it)?.apply { list.add(this) }
                }
                if (currentTask.getTaskType() == TaskType.FORMAL && newStatus == TaskStatus.DECLINED) {
                    updatePeersOnFormalDeclineOrRework(it)?.apply { list.add(this) }
                }
            }
        }
    }

    fun taskUpdateActivityPriority(taskStatus: TaskStatus): ActivityPriority {
        return when (taskStatus) {
            TaskStatus.APPROVED, TaskStatus.COMPLETE, TaskStatus.ENDORSED -> {
                ActivityPriority.SUCCESS
            }
            TaskStatus.REWORK, TaskStatus.WITHDRAWN, TaskStatus.ABSTAINED, TaskStatus.PENDING -> {
                ActivityPriority.WARNING
            }
            TaskStatus.DECLINED, TaskStatus.OUTDATED -> {
                ActivityPriority.ERROR
            }
            else -> { ActivityPriority.INFO }
        }
    }

    fun taskUpdateActivityStatus(taskStatus: TaskStatus): ActivityStatus {
        return when (taskStatus) {
            TaskStatus.APPROVED -> { ActivityStatus.APPROVED }
            TaskStatus.ENDORSED -> { ActivityStatus.ENDORSED }
            TaskStatus.WITHDRAWN, TaskStatus.OUTDATED -> { ActivityStatus.WITHDREW }
            TaskStatus.DECLINED -> { ActivityStatus.DECLINED }
            else -> { ActivityStatus.UPDATED }
        }
    }

    private fun updatePeersOnFormalDeclineOrRework(peerTask: Task): TaskUpdate? {
        return if (peerTask.getTaskType() == TaskType.FORMAL &&
            peerTask.getTaskStatus() == TaskStatus.APPROVED
        ) {
            outdated(peerTask)
        } else if (peerTask.getTaskType() == TaskType.INFORMAL &&
            (
                peerTask.getTaskStatus() == TaskStatus.ENDORSED ||
                    peerTask.getTaskStatus() == TaskStatus.ABSTAINED
                )
        ) {
            outdated(peerTask)
        } else if ((peerTask.getTaskType() == TaskType.INFORMAL || peerTask.getTaskType() == TaskType.FORMAL) &&
            peerTask.getTaskStatus() == TaskStatus.PENDING
        ) {
            withdrawn(peerTask)
        } else null
    }

    private fun updatePeersOnInformalRework(peerTask: Task): TaskUpdate? {
        return if (peerTask.getTaskType() == TaskType.INFORMAL &&
            (
                peerTask.getTaskStatus() == TaskStatus.ENDORSED ||
                    peerTask.getTaskStatus() == TaskStatus.ABSTAINED
                )
        ) {
            outdated(peerTask)
        } else if ((peerTask.getTaskType() == TaskType.INFORMAL || peerTask.getTaskType() == TaskType.FORMAL) &&
            (peerTask.getTaskStatus() == TaskStatus.PENDING)
        ) {
            withdrawn(peerTask)
        } else null
    }

    private fun withdrawn(peerTask: Task) = TaskUpdate().apply {
        setTaskStatus(TaskStatus.WITHDRAWN)
        note = WITHDRAWN_NOTE
        task = peerTask
    }

    private fun outdated(peerTask: Task) = TaskUpdate().apply {
        setTaskStatus(TaskStatus.OUTDATED)
        note = OUTDATED_NOTE
        task = peerTask
    }
}
