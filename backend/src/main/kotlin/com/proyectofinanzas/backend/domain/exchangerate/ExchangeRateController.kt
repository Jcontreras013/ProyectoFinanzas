package com.proyectofinanzas.backend.domain.exchangerate

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/exchange-rates")
class ExchangeRateController(
    private val exchangeRateService: ExchangeRateService,
) {
    @GetMapping
    fun list(): List<ExchangeRateResponse> = exchangeRateService.list()

    @PostMapping
    fun upsert(@Valid @RequestBody request: ExchangeRateRequest): ExchangeRateResponse =
        exchangeRateService.upsert(request)
}
