/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.TaskStatus
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents
import com.haulmont.cuba.core.global.DeletePolicy

@PublishEntityChangedEvents
@Table(name = "task_update")
@Entity(name = "TaskUpdate")
open class TaskUpdate : StandardEntity() {
    companion object {
        private const val serialVersionUID = 6475371636081592857L
    }

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    var task: Task? = null

    @Column(name = "status")
    var status: String? = null

    fun getTaskStatus(): TaskStatus? = status?.let { TaskStatus.fromId(it) }

    fun setTaskStatus(status: TaskStatus?) {
        this.status = status?.id
    }

    @Column(name = "note")
    var note: String? = null
}
