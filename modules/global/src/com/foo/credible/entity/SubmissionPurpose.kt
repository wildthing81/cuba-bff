/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.haulmont.cuba.core.entity.StandardEntity

@Table(name = "submission_purpose")
@Entity(name = "SubmissionPurpose")
open class SubmissionPurpose : StandardEntity() {

    @NotNull
    // @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    var submission: Submission? = null

    @NotNull
    @Lob
    @Column(name = "type", nullable = false)
    var type: String? = null

    @NotNull
    @Lob
    @Column(name = "purpose", nullable = false)
    var purpose: String? = null

    @Column(name = "is_primary")
    var isPrimary: Boolean? = null

    companion object {
        private const val serialVersionUID = -2680775575091924585L
    }
}
