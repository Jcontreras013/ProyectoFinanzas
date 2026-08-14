package com.proyectofinanzas.backend.domain.payment

import com.proyectofinanzas.backend.common.Currency
import com.proyectofinanzas.backend.common.CreatedOnlyEntity
import com.proyectofinanzas.backend.domain.expense.Expense
import com.proyectofinanzas.backend.domain.invoice.Invoice
import com.proyectofinanzas.backend.domain.journal.JournalEntry
import com.proyectofinanzas.backend.domain.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Generated
import org.hibernate.generator.EventType
import java.math.BigDecimal
import java.time.LocalDate

enum class PaymentMethod {
    CASH,
    BANK,
}

/** Cobro de una factura (CxC) o pago de un gasto a crédito (CxP). Exactamente uno de los dos. */
@Entity
@Table(name = "payments")
class Payment(
    @Generated(event = [EventType.INSERT])
    @Column(name = "payment_number", nullable = false, updatable = false, insertable = false)
    var paymentNumber: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    var invoice: Invoice? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    var expense: Expense? = null,

    @Column(nullable = false, precision = 19, scale = 4)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    var currency: Currency,

    @Column(name = "exchange_rate", nullable = false, precision = 12, scale = 6)
    var exchangeRate: BigDecimal,

    @Column(name = "amount_in_base", nullable = false, precision = 19, scale = 4)
    var amountInBase: BigDecimal,

    @Column(name = "payment_date", nullable = false)
    var paymentDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var method: PaymentMethod,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id")
    var journalEntry: JournalEntry? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: User,
) : CreatedOnlyEntity()
