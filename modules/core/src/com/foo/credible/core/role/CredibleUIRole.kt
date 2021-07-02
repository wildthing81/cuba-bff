/*
 * The code is copyright ©2021
 */

package com.foo.credible.core.role

import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition
import com.haulmont.cuba.security.app.role.annotation.EntityAccess
import com.haulmont.cuba.security.app.role.annotation.EntityAccessContainer
import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccess
import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccessContainer
import com.haulmont.cuba.security.app.role.annotation.Role
import com.haulmont.cuba.security.app.role.annotation.SpecificAccess
import com.haulmont.cuba.security.entity.EntityOp
import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer
import com.haulmont.cuba.security.role.EntityPermissionsContainer
import com.haulmont.cuba.security.role.SpecificPermissionsContainer

@Role(name = com.foo.credible.core.role.CredibleUIRole.Companion.NAME, isDefault = true, securityScope = "REST")
class CredibleUIRole : AnnotatedRoleDefinition() {
    companion object {
        const val NAME = "credible-ui"
    }

    @EntityAccessContainer(
        EntityAccess(
            entityName = "Institution",
            operations =
            [EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE, EntityOp.DELETE]
        ),
        EntityAccess(
            entityName = "sec\$User",
            operations =
            [EntityOp.CREATE, EntityOp.READ, EntityOp.UPDATE, EntityOp.DELETE]
        )
    )
    override fun entityPermissions(): EntityPermissionsContainer {
        return super.entityPermissions()
    }

    @SpecificAccess(permissions = ["cuba.restApi.enabled"])
    override fun specificPermissions(): SpecificPermissionsContainer {
        return super.specificPermissions()
    }

    @EntityAttributeAccessContainer(
        EntityAttributeAccess(
            entityName = "sec\$User",
            view = arrayOf("*"),
            modify = arrayOf("*")
        )
    )
    override fun entityAttributePermissions(): EntityAttributePermissionsContainer {
        return super.entityAttributePermissions()
    }

    override fun getLocName(): String? {
        return "CredibleUI user role"
    }
}
