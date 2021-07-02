/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Lob
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.global.DeletePolicy

@Table(name = "institution")
@javax.persistence.Entity(name = "Institution")
open class Institution : StandardEntity() {
    companion object {
        private const val serialVersionUID = -4927592392803259081L
    }

    @NotNull
    @Column(name = "name", nullable = false)
    var name: String? = null

    @NotNull
    @Column(name = "site_id", nullable = false, unique = true)
    var siteId: String? = null

    @Lob
    @NotNull
    @Column(name = "configuration", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var configuration: String? = null

    @Lob
    @NotNull
    @Column(name = "borrowerDefaults", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var borrowerDefaults: String? = null

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @OneToMany(mappedBy = "institution")
    var borrowers: MutableSet<Borrower>? = mutableSetOf()
}
