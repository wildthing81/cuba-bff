/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.cuba.core.entity.StandardEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Convert
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Table(name = "activity")
@Entity(name = "Activity")
class Activity : StandardEntity() {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution? = null

    @NotNull
    @Column(name = "id_key", nullable = false, unique = false)
    var idKey: String? = null

    @NotNull
    @Column(name = "id_value", nullable = false, unique = false)
    var idValue: String? = null

    @Lob
    @NotNull
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var details: String? = null

    companion object {
        private const val serialVersionUID = -5094811947802424132L
    }
}
