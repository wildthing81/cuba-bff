/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import com.haulmont.cuba.core.entity.StandardEntity
import javax.persistence.Column
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Table(name = "submission_type")
@javax.persistence.Entity(name = "SubmissionType")
class SubmissionType : StandardEntity() {

    @NotNull
    @Column(name = "type", nullable = false)
    var type: String? = null

    companion object {
        private const val serialVersionUID = 273330087178897339L
    }
}
