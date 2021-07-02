package com.foo.credible.exceptionhandlers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto

@ControllerAdvice
class OCPRequestExceptionHandler {

    /**
     * Handling invalid token exception
     *
     * @param request
     * @return ResponseEntity with ErrorDto
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(request: WebRequest?): ResponseEntity<Any>? {
        val body = ErrorDto(
            "OCPRequest",
            request!!.sessionId,
            ErrorConstants.UNAUTHORIZED,
            "Invalid token"
        )
        return ResponseEntity(body, HttpStatus.UNAUTHORIZED)
    }
}
