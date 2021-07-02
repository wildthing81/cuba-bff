/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.dto.TaskDto
import com.fasterxml.jackson.databind.JsonNode

interface TaskService {

    companion object {
        const val NAME = "crd_TaskService"
    }

    fun createTask(taskDto: TaskDto): Any

    fun fetchTask(taskId: String): Any

    fun fetchTasks(assigned: Boolean = false, pathVar: String?): Any

    fun updateTask(taskId: String, requestBody: JsonNode): Any

    fun flaggedForUser(taskId: String, flag: Boolean): Any
}
