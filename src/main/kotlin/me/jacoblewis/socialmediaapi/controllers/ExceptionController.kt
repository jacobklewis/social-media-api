package me.jacoblewis.socialmediaapi.controllers

import me.jacoblewis.socialmediaapi.exceptions.BadRequestException
import me.jacoblewis.socialmediaapi.exceptions.ConflictException
import me.jacoblewis.socialmediaapi.exceptions.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionController : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BadRequestException::class, ConflictException::class, NotFoundException::class)
    fun errorToProjects(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        return when (ex) {
            is BadRequestException -> {
                handleExceptionInternal(
                    ex,
                    mapOf("message" to ex.message, "status" to 400),
                    HttpHeaders(),
                    HttpStatus.BAD_REQUEST,
                    request
                )
            }
            is ConflictException -> {
                handleExceptionInternal(ex, mapOf("message" to ex.message, "status" to 409), HttpHeaders(), HttpStatus.CONFLICT, request)
            }
            is NotFoundException -> {
                handleExceptionInternal(
                    ex,
                    mapOf("message" to ex.message, "status" to 404),
                    HttpHeaders(),
                    HttpStatus.NOT_FOUND,
                    request
                )
            }
            else -> handleExceptionInternal(ex, ex.message, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
        }
    }

}