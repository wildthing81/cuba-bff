/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.ViewConstants.TASK_FETCH
import com.anzi.credible.constants.ViewConstants.TASK_UPDATE
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Task
import com.anzi.credible.helpers.TaskHelper
import com.anzi.credible.repository.SubmissionRepository
import com.anzi.credible.repository.TaskRepository
import com.anzi.credible.utils.AppUtils.then
import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import java.util.Date

@Service(TaskService.NAME)
open class TaskServiceBean : TaskService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var submissionRepository: SubmissionRepository

    @Inject
    private lateinit var taskRepository: TaskRepository

    /**
     * Fetch task by Id
     *
     * @param taskId
     * @return TaskDto or ErrorDto
     */
    override fun fetchTask(taskId: String) = try {
        log.info { "Fetching task: $taskId" }
        taskRepository.fetchTaskById(taskId, TASK_FETCH).let { task ->
            val loggedUser = userService.fetchLoggedUser()
            task.viewedUsers?.none { loggedUser == it }?.then {
                updateTaskViewStatusOnFetch(task, loggedUser)
            }
            TaskHelper.buildDto(task, loggedUser, userService.fetchUsersByLogin(listOf(task.createdBy))[0])
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "No task exists for: $taskId" }
        ErrorDto(
            Task::class.simpleName!!,
            taskId,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch user created/assigned tasks or count if path variable is non-null
     *
     * @return list of tasks
     */
    override fun fetchTasks(assigned: Boolean, pathVar: String?) = try {
        val loggedUser = userService.fetchLoggedUser()
        log.info { if (assigned) "Fetching assigned tasks for user" else "Fetching tasks created by user" }
        taskRepository.fetchTasks(assigned, loggedUser).let { tasks ->
            if (pathVar.isNullOrEmpty()) tasks.sortedByDescending { it.updateTs }
                .map { task ->
                    TaskHelper.buildDto(
                        task,
                        loggedUser,
                        userService.fetchUsersByLogin(listOf(task.createdBy))[0]
                    )
                }
            else getTaskCount(tasks, loggedUser)
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "No tasks" }
        ErrorDto(
            Task::class.simpleName!!,
            null,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * service method for 'update task' endpoint
     *
     * @return TaskDto or ErrorDto
     */
    override fun updateTask(taskId: String, requestBody: JsonNode) = try {
        log.info { "Updating task: $taskId" }
        val status = TaskStatus.fromId(requestBody.get("status")?.asText())
        val note = requestBody.get("note")?.asText()
        taskRepository.fetchTaskById(taskId, TASK_UPDATE).let {
            TaskHelper.createTaskUpdateEntities(it, status, note)
        }.run {
            val entitySet = taskRepository.createTaskUpdates(this)
            log.info { "Task: $taskId updated!" }
            log.info { "Total updates: ${entitySet?.size}" }
        }
        AppConstants.SUCCESS
    } catch (dbe: Exception) {
        log.error(dbe) { "Error updating task: $taskId" }
        ErrorDto(
            Task::class.simpleName!!,
            taskId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Flag Task for user
     *
     * @param taskId
     * @param flag
     * @return
     */
    override fun flaggedForUser(taskId: String, flag: Boolean) = try {
        val loggedUser = userService.fetchLoggedUser()
        log.info { "Task: $taskId, isFlagged: $flag for User: ${loggedUser.login}" }
        taskRepository.fetchTaskById(taskId, TASK_UPDATE).let {
            if (flag) it.flaggedUsers!!.add(loggedUser) else it.flaggedUsers!!.remove(loggedUser)

            taskRepository.updateTask(it)
            AppConstants.SUCCESS
        }
    } catch (dbe: Exception) {
        log.error { "Error flagging task: $taskId for user" }
        ErrorDto(
            Task::class.simpleName!!,
            taskId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Create a task
     *
     * @param taskDto
     * @return TaskDto or ErrorDto
     * @throws
     */
    override fun createTask(taskDto: TaskDto): Any {
        log.info { "Creating a new task for submission: ${taskDto.submissionId}" }
        val loggedUser = userService.fetchLoggedUser()

        return try {
            submissionRepository.fetchSubmissionById(taskDto.submissionId!!)
                .let {
                    taskRepository.createTask(it!!, taskDto)
                }.let {
                    log.info { "New task ${it.id}" }
                    TaskHelper.buildDto(it, loggedUser, userService.fetchUsersByLogin(listOf(it.createdBy))[0])
                }
        } catch (dbe: Exception) {
            log.error(dbe) { "Error creating new task for submission: ${taskDto.submissionId}" }
            ErrorDto(
                Task::class.simpleName!!,
                null,
                ErrorConstants.CREATE_ENTITY,
                dbe.message
            )
        }
    }

    private fun updateTaskViewStatusOnFetch(task: Task, loggedUser: AppUser) {
        task.viewedUsers?.add(loggedUser)
        taskRepository.updateTask(task)
    }

    /**
     * Returns count of tasks that are either not viewed by user or overdue.
     *
     * @param tasks
     * @param loggedUser
     * @return
     */
    private fun getTaskCount(tasks: List<Task>, loggedUser: AppUser): Int {
        var count = 0
        tasks.forEach { task ->
            if (task.viewedUsers?.none { user -> loggedUser == user }!! or task.due?.before(Date())!!) {
                count++
            }
        }
        return count
    }
}
