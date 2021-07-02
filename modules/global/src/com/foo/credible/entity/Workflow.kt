/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.SubmissionStatus
import com.anzi.credible.converters.StringToListConverter
import com.haulmont.chile.core.annotations.Composition
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.global.DeletePolicy

@Table(name = "workflow")
@javax.persistence.Entity(name = "Workflow")
open class Workflow : StandardEntity() {
    companion object {
        private const val serialVersionUID = -4564438973864178801L
    }

    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    var name: String? = null

    @NotNull
    @Column(name = "submission_types", nullable = false)
    @Convert(converter = StringToListConverter::class)
    var submissionTypes: MutableList<String>? = mutableListOf()

    @NotNull
    @Column(name = "initial_status", nullable = false)
    var initialStatus: String? = null

    fun getInitialStatus(): SubmissionStatus? = initialStatus?.let { SubmissionStatus.fromId(it) }

    fun setInitialStatus(status: SubmissionStatus?) {
        this.initialStatus = status?.id!!
    }

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "workflow")
    @OrderBy("index")
    var steps: MutableList<WorkflowStep>? = mutableListOf()

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution? = null
}
