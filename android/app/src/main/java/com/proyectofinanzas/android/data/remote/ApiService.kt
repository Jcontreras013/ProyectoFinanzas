package com.proyectofinanzas.android.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("accounts")
    suspend fun listAccounts(): List<AccountDto>

    @GET("journal-entries")
    suspend fun listJournalEntries(@Query("page") page: Int, @Query("size") size: Int): PageResponse<JournalEntryDto>

    @GET("journal-entries/{id}")
    suspend fun getJournalEntry(@Path("id") id: String): JournalEntryDto

    @POST("journal-entries")
    suspend fun createJournalEntry(@Body request: CreateJournalEntryRequest): JournalEntryDto

    @GET("invoices")
    suspend fun listInvoices(@Query("page") page: Int, @Query("size") size: Int): PageResponse<InvoiceDto>

    @GET("invoices/{id}")
    suspend fun getInvoice(@Path("id") id: String): InvoiceDto

    @GET("expenses")
    suspend fun listExpenses(@Query("page") page: Int, @Query("size") size: Int): PageResponse<ExpenseDto>

    @GET("expenses/{id}")
    suspend fun getExpense(@Path("id") id: String): ExpenseDto

    @POST("expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): ExpenseDto

    @GET("reports/dashboard-kpis")
    suspend fun dashboardKpis(): DashboardKpisDto
}
