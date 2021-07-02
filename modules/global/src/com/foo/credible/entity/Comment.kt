/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents
import com.haulmont.cuba.core.global.DeletePolicy
import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull

@PublishEntityChangedEvents
@Table(name = "comment")
@javax.persistence.Entity(name = "Comment")
class Comment : StandardEntity() {

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    var submission: Submission? = null

    @NotNull
    @Column(name = "text", nullable = false)
    var text: String? = null

    companion object {
        private const val serialVersionUID = 1342693338264304249L
    }
}
