/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.entity.StandardEntity

interface StandardEntityListener <T : StandardEntity> {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun beforeEntityCommit(event: EntityChangedEvent<T, UUID>)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun afterEntityCommit(event: EntityChangedEvent<T, UUID>)
}
