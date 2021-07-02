/*
 * The code is copyright ©2021
 */

package com.foo.credible.entity

import java.util.Date
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import com.anzi.credible.converters.StringToJsonBConverter
import com.haulmont.cuba.core.entity.annotation.Extends
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse
import com.haulmont.cuba.core.global.DeletePolicy
import com.haulmont.cuba.security.entity.User

@Entity(name = "AppUser")
@Extends(User::class)
open class AppUser : User() {
    companion object {
        private const val serialVersionUID = -2981637521268674929L
    }

    @Lob
    @Column(name = "profileImage")
    var profileImage: String? = null

    @NotNull
    @Column(name = "scope")
    var scope: String? = null

    @NotNull
    @Column(name = "cad_level")
    var cadLevel: Int? = null

    @Column(name = "email_verified")
    var isEmailVerified: Boolean? = null

    @Lob
    @Column(name = "preferences", columnDefinition = "jsonb")
    @Convert(converter = StringToJsonBConverter::class)
    var preferences: String? = null

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "institution_id")
    var institution: Institution? = null

    @Column(name = "last_notified_ts")
    var lastNotifiedAt: Date? = null
}
