/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.constants.ViewConstants.TASK_FETCH
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.TaskUpdate
import com.haulmont.cuba.core.global.EntitySet
import com.haulmont.cuba.security.entity.User

interface TaskRepository {

    fun fetchTaskById(taskId: String, viewName: String = TASK_FETCH): Task

    fun createTask(submission: Submission, taskDto: TaskDto): Task

    fun createTaskUpdates(updateList: List<TaskUpdate>): EntitySet?

    fun updateTask(task: Task): Task

    // fun flaggedForUser(task: Task, flag: Boolean, loggedUser: User): EntitySet?

    fun fetchTasks(assigned: Boolean, loggedUser: User, viewName: String = TASK_FETCH,): List<Task>
}
