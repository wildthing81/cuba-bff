/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.cuba.core.entity.StandardEntity

@Table(name = "submission_section")
@Entity(name = "SubmissionSection")
open class SubmissionSection : StandardEntity() {

    @NotNull
    @Column(name = "slug", nullable = false)
    var slug: String? = null

    @NotNull
    // @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id")
    var submission: Submission? = null

    @NotNull
    @Lob
    @Column(name = "content", nullable = false)
    var content: String? = null

    @Lob
    @Column(name = "exceptions", columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var exceptions: String? = null

    @Lob
    @Column(name = "comments")
    var comments: String? = null

    companion object {
        private const val serialVersionUID = 2573083770287045188L
    }
}
