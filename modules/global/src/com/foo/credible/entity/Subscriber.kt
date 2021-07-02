/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import com.haulmont.cuba.core.entity.BaseUuidEntity

@Table(name = "subscriber")
@Entity(name = "Subscriber")
class Subscriber : BaseUuidEntity() {
    @Column(name = "is_read")
    var isRead: Boolean? = null

    @Column(name = "is_hidden")
    var isHidden: Boolean? = null

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id")
    var notification: Notification? = null

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var user: AppUser? = null

    companion object {
        private const val serialVersionUID = 1342693338264304249L
    }
}
