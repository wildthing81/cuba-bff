/*
 * The code is copyright ©2021
 */

package com.foo.credible.exceptions

class CrdWebSocketException : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
