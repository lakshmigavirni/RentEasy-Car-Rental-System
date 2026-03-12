package com.PaymentService.Service.strategy;

import com.PaymentService.DTOs.PaymentRequest;
import com.PaymentService.DTOs.StripeSessionRequest;
import com.PaymentService.Entity.Payment;

import java.math.BigDecimal;

/**
 * Strategy interface for payment-method-specific session building.
 *
 * Each implementation handles one payment method (e.g. "card", "upi").
 * Spring auto-builds a Map<String, PaymentStrategy> keyed by bean name,
 * which PaymentServiceImpl uses to select the right strategy.
 *
 * The strategy's ONLY job: populate a StripeSessionRequest.
 * PaymentServiceImpl owns: the Stripe call, entity save, mock auto-complete, and response.
 */
public interface PaymentStrategy {

    /** Token this strategy handles — must match the @Component bean name. */
    String supportedMethod();

    /**
     * Fill in payment-method-specific fields on {@code sessionRequest}.
     *
     * @param sessionRequest  blank request to populate
     * @param payReq          original API request from caller
     * @param payment         Payment entity (common fields already set, not yet saved)
     * @param amount          amount already converted to target currency
     * @param successUrl      configured success redirect URL
     * @param cancelUrl       configured cancel redirect URL
     * @param bookingRef      booking reference string
     */
    void buildSession(
            StripeSessionRequest sessionRequest,
            PaymentRequest payReq,
            Payment payment,
            BigDecimal amount,
            String successUrl,
            String cancelUrl,
            String bookingRef
    );
}
