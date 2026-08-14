package com.proyectofinanzas.backend.common

class NotFoundException(message: String) : RuntimeException(message)

class BusinessRuleException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)
