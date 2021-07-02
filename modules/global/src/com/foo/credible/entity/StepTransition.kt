/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.constants.SubmissionStatus
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.global.DeletePolicy

@Table(name = "step_transition")
@javax.persistence.Entity(name = "StepTransition")
open class StepTransition : StandardEntity() {
    companion object {
        private const val serialVersionUID = 7688063631704398014L
    }

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id", nullable = false)
    var step: WorkflowStep? = null

    @NotNull
    @Column(name = "to_step_index", nullable = false)
    var toStepIndex: Int? = null

    @NotNull
    @Column(name = "trigger", nullable = false)
    var trigger: String? = null

    fun getStepTrigger(): StepTrigger? = trigger?.let { StepTrigger.fromId(it) }

    fun setStepTrigger(trigger: StepTrigger?) {
        this.trigger = trigger?.id!!
    }

    @NotNull
    @Column(name = "submission_status", nullable = false)
    var submissionStatus: String? = null

    fun getStatus(): SubmissionStatus? = submissionStatus?.let { SubmissionStatus.fromId(it) }

    fun setStatus(status: SubmissionStatus?) {
        this.submissionStatus = status?.id!!
    }

    @NotNull
    @Column(name = "label", nullable = false)
    var label: String? = null

    @NotNull
    @Column(name = "borrower_refresh", nullable = false)
    var borrowerRefresh: Boolean? = false
}
