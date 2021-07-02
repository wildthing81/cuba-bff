/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.TaskUpdate
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.repository.TaskRepository
import com.anzi.credible.repository.UserRepository
import com.anzi.credible.utils.DateUtils.toUTC
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.EntitySet
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

@Repository
open class TaskRepositoryImpl : TaskRepository {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var dataManager: DataManager

    /**
     * query database to fetch task by id
     *
     * @param taskId
     * @param viewName
     * @return
     */
    override fun fetchTaskById(taskId: String, viewName: String): Task = dataManager
        .load(Task::class.java)
        .id(UUID.fromString(taskId)).view(viewName).one()

    /**
     * query database to fetch tasks created by/assigned to user
     *
     * @param assigned
     * @param viewName
     * @param loggedUser
     * @return
     */
    override fun fetchTasks(assigned: Boolean, loggedUser: User, viewName: String): MutableList<Task> = dataManager
        .load(Task::class.java)
        .query(
            if (assigned) QueryConstants.FETCH_ASSIGNED_TASKS
            else QueryConstants.FETCH_CREATED_TASKS
        ).parameter("user", if (assigned) loggedUser else loggedUser.login)
        .view(viewName).list()

    /**
     * query to commit new task
     *
     * @param submission
     * @param taskDto
     * @return
     */
    override fun createTask(
        submission: Submission,
        taskDto: TaskDto,
    ): Task {

        val commitContext = CommitContext()
        val task = dataManager.create(Task::class.java)
        commitContext.addInstanceToCommit(task)

        addDetails(task, taskDto)
        createAssociations(task, submission)
        dataManager.commit(commitContext)

        taskDto.taskId = task.id
        taskDto.submissionId = submission.id.toString()
        return task
    }

    /**
     *  commit status/note TaskUpdate entities
     *
     * @param updateList
     * @return
     */
    override fun createTaskUpdates(updateList: List<TaskUpdate>): EntitySet = CommitContext().run {
        updateList.forEach {
            log.debug { "Task Id ${it.task}, Status ${it.status}, Updated By ${it.updatedBy}" }
        }
        setCommitInstances(updateList)
        dataManager.commit(this)
    }

    /**
     * Update Task attributes
     *
     * @param task
     * @return
     */
    override fun updateTask(task: Task): Task = dataManager.commit(task)

    /*
    override fun flaggedForUser(task: Task, flag: Boolean, loggedUser: User): EntitySet = CommitContext().run {
        log.info { "Task Id ${task.id}, isFlagged: $flag for ${loggedUser.login}" }
        task.apply {
            if (flag) flaggedUsers!!.add(loggedUser) else flaggedUsers!!.remove(loggedUser)
        }
        dataManager.commit(this.addInstanceToCommit(task))
    }*/

    private fun addDetails(
        task: Task,
        taskDto: TaskDto,
    ) = task.apply {
        category = taskDto.category
        due = taskDto.due!!.toUTC()
        type = taskDto.type!!
        description = taskDto.description
        assignee = userRepository.getUsersFromIds(listOf(taskDto.assignee as String))[0]
        setTaskStatus(TaskStatus.PENDING)
    }

    private fun createAssociations(
        task: Task,
        submission: Submission,
    ) {
        task.submission = submission
    }
}
