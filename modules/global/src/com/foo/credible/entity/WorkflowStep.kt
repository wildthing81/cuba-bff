/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.chile.core.annotations.Composition
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.global.DeletePolicy

@Table(name = "workflow_step")
@javax.persistence.Entity(name = "WorkflowStep")
open class WorkflowStep : StandardEntity() {
    companion object {
        private const val serialVersionUID = 4004225287190398940L
    }

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: Workflow? = null

    @NotNull
    @Column(name = "index")
    var index: Int? = null

    @NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "step")
    var transitions: MutableList<StepTransition>? = null

    @Lob
    @NotNull
    @Column(name = "layout", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var layout: String? = null
}
