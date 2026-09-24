package com.example.service.payment

enum class PaymentProviderType {
    STRIPE,
    MOBILE_MONEY,
    PAYMENT_LINK,
    CASH_ON_DELIVERY
}

data class PaymentCheckoutResult(
    val paymentId: String,
    val provider: PaymentProviderType,
    val paymentUrl: String,
    val status: String, // PENDING, PAID, FAILED
    val transactionRef: String
)

class PaymentGatewayService {

    fun generatePaymentLink(
        provider: PaymentProviderType,
        orderId: String,
        amount: Double,
        currency: String,
        customerPhone: String
    ): PaymentCheckoutResult {
        val txRef = "TXN-${System.currentTimeMillis().toString().takeLast(6)}"
        val url = when (provider) {
            PaymentProviderType.STRIPE -> "https://checkout.stripe.com/pay/$orderId?ref=$txRef"
            PaymentProviderType.MOBILE_MONEY -> "https://pay.wave.com/m/$txRef"
            PaymentProviderType.PAYMENT_LINK -> "https://pay.nexstore.com/order/$orderId"
            PaymentProviderType.CASH_ON_DELIVERY -> "https://nexstore.com/cod-confirmation/$orderId"
        }

        return PaymentCheckoutResult(
            paymentId = "pay_${System.currentTimeMillis()}",
            provider = provider,
            paymentUrl = url,
            status = if (provider == PaymentProviderType.CASH_ON_DELIVERY) "CONFIRMED_COD" else "PENDING",
            transactionRef = txRef
        )
    }
}
