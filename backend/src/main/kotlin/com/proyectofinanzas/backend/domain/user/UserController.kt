package com.proyectofinanzas.backend.domain.user

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    fun list(): List<UserResponse> = userService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): UserResponse = userService.get(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): UserResponse = userService.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateUserRequest): UserResponse =
        userService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@PathVariable id: UUID) = userService.deactivate(id)
}
