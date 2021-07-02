/*
 * The code is copyright ©2021
 */

package com.foo.credible.dto

import javax.websocket.Session
import com.haulmont.cuba.security.entity.User

data class NotificationSession(val user: User, val session: Session)
