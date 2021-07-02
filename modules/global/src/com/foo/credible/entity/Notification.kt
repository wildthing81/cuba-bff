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
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.anzi.credible.constants.NotificationType
import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.chile.core.annotations.Composition
import com.haulmont.cuba.core.entity.StandardEntity
import com.haulmont.cuba.core.entity.annotation.OnDelete
import com.haulmont.cuba.core.entity.annotation.PublishEntityChangedEvents
import com.haulmont.cuba.core.global.DeletePolicy

@PublishEntityChangedEvents
@Table(name = "notification")
@Entity(name = "Notification")
class Notification : StandardEntity() {

    @NotNull
    @Column(name = "text", nullable = false)
    var text: String? = null

    @Lob
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    @Convert(converter = StringToJsonBConverter::class)
    var payload: String? = null

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "notification")
    var subscribers: MutableList<Subscriber> = mutableListOf()

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    var institution: Institution? = null

    @NotNull
    @Column(name = "type", nullable = false)
    var type: String? = null

    fun getNotificationType(): NotificationType? = type?.let { NotificationType.fromId(it) }

    fun setNotificationType(type: NotificationType?) {
        this.type = type?.id
    }
    companion object {
        private const val serialVersionUID = 1342693338264304249L
    }
}
