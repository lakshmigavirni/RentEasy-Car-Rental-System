package com.PaymentService.Service.strategy;

import com.PaymentService.DTOs.PaymentRequest;
import com.PaymentService.DTOs.StripeSessionRequest;
import com.PaymentService.Entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy for paymentMethod = "card".
 *
 * Populates StripeSessionRequest with card-specific fields.
 * No repositories, no Stripe calls — all that stays in PaymentServiceImpl.
 *
 * @Component("card") makes Spring register this bean with key "card"
 * so it lands in Map<String, PaymentStrategy> automatically.
 */
@Component("card")
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public String supportedMethod() {
        return "card";
    }

    @Override
    public void buildSession(
            StripeSessionRequest sessionRequest,
            PaymentRequest payReq,
            Payment payment,
            BigDecimal amount,
            String successUrl,
            String cancelUrl,
            String bookingRef
    ) {
        sessionRequest.setAmount(amount);
        sessionRequest.setCurrency(payReq.getCurrency());
        sessionRequest.setCustomerEmail(payReq.getCustomerEmail());
        sessionRequest.setDescription(payment.getDescription());
        sessionRequest.setSuccessUrl(successUrl);
        sessionRequest.setCancelUrl(cancelUrl);
        sessionRequest.setBookingReference(bookingRef);
        // card-specific: no additional fields beyond the common ones above
    }
}
