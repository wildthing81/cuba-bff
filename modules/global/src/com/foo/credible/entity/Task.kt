/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.TaskCategory
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.TaskType
import com.haulmont.chile.core.annotations.Composition
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents
import com.haulmont.cuba.core.global.DeletePolicy
import com.haulmont.cuba.security.entity.User

@PublishEntityChangedEvents
@Table(name = "task")
@Entity(name = "Task")
open class Task : StandardEntity() {
    companion object {
        private const val serialVersionUID = -8006712203223777158L
    }

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    var submission: Submission? = null

    @NotNull
    @Column(name = "category", nullable = false)
    var category: String? = null

    fun getTaskCategory(): TaskCategory? = category?.let { TaskCategory.fromId(it) }

    fun setTaskCategory(category: TaskCategory?) {
        this.category = category?.id
    }

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    var assignee: User? = null

    @NotNull
    @Column(name = "type", nullable = false)
    var type: String? = null

    fun getTaskType(): TaskType? = type?.let { TaskType.fromId(it) }

    fun setTaskType(type: TaskType?) {
        this.type = type?.id
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date")
    var due: Date? = null

    @Lob
    @Column(name = "description")
    var description: String? = null

    @Lob
    @Column(name = "note")
    var note: String? = null

    @NotNull
    @Column(name = "status", nullable = false)
    var status: String? = null

    fun getTaskStatus(): TaskStatus? = status?.let { TaskStatus.fromId(it) }

    fun setTaskStatus(status: TaskStatus?) {
        this.status = status?.id
    }

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "task")
    var taskUpdates: MutableList<TaskUpdate>? = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "TASK_FLAGGED_USERS",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var flaggedUsers: MutableSet<User>? = mutableSetOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "TASK_VIEWED_USERS",
        joinColumns = [JoinColumn(name = "task_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var viewedUsers: MutableSet<User>? = mutableSetOf()
}
