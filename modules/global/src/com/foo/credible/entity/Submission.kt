/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import java.util.Date
import javax.persistence.*
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.SubmissionStatus
import com.haulmont.chile.core.annotations.Composition
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents
import com.haulmont.cuba.core.global.DeletePolicy
import com.haulmont.cuba.security.entity.User

@PublishEntityChangedEvents
@Table(name = "submission")
@Entity(name = "Submission")
open class Submission : StandardEntity() {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "due_date")
    var due: Date? = null

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "submission")
    var submissionPurpose: MutableList<SubmissionPurpose> = mutableListOf()

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "submission")
    var sections: MutableList<SubmissionSection> = mutableListOf()

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "submission")
    var tasks: MutableList<Task> = mutableListOf()

    @Lob
    @Column(name = "note")
    var note: String? = null

    @NotNull
    @Column(name = "status", nullable = false)
    var status: String? = null

    fun getStatus(): SubmissionStatus? = status?.let { SubmissionStatus.fromId(it) }

    fun setStatus(status: SubmissionStatus?) {
        this.status = status?.id!!
    }

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    var borrower: Borrower? = null

    @OnDeleteInverse(DeletePolicy.DENY)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    var workflow: Workflow? = null

    @NotNull
    @Column(name = "workflow_version")
    var workflowVersion: Int? = null

    @NotNull
    @Column(name = "workflow_step")
    var workflowStep: Int? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "SUBMISSION_TEAM",
        joinColumns = [JoinColumn(name = "submission_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var team: MutableSet<AppUser> = mutableSetOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "SUBMISSION_FLAGGED_USERS",
        joinColumns = [JoinColumn(name = "submission_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var flaggedUsers: MutableSet<User> = mutableSetOf()

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "submission")
    var comments: MutableList<Comment> = mutableListOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "SUBMISSION_VIEWED_USERS",
        joinColumns = [JoinColumn(name = "submission_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var viewedUsers: MutableSet<User>? = mutableSetOf()

    @Column(name = "display_ref", unique = true, length = 10)
    var displayRef: String? = null

    companion object {
        private const val serialVersionUID = 4229763916840896627L
    }
}
