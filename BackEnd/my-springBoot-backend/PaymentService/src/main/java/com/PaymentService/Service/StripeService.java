package com.PaymentService.Service;

import com.PaymentService.DTOs.StripeSessionRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true")
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Returns true when running with a dummy/placeholder API key.
     * In mock mode the real Stripe API is never called.
     */
    private boolean isMockMode() {
        return apiKey == null
                || (!apiKey.startsWith("sk_test_") && !apiKey.startsWith("sk_live_"));
    }

    public Session createCheckoutSession(StripeSessionRequest request) throws StripeException {

        // ── MOCK MODE (no real Stripe account) ──────────────────────────────────
        if (isMockMode()) {
            log.warn("Stripe mock mode active – returning simulated session. " +
                     "Set a real sk_test_... key to use live Stripe.");

            String fakeSessionId = "mock_session_" + UUID.randomUUID().toString().replace("-", "");
            String targetUrl = (request.getSuccessUrl() != null ? request.getSuccessUrl() : successUrl)
                               + "?session_id=" + fakeSessionId + "&mock=true";

            // Build a Session object via the raw-JSON constructor so we don't
            // need to hit Stripe's API at all.
            Session mockSession = new Session();
            mockSession.setId(fakeSessionId);
            mockSession.setUrl(targetUrl);
            mockSession.setStatus("open");
            return mockSession;
        }

        // ── REAL STRIPE MODE ─────────────────────────────────────────────────────
        long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.getSuccessUrl() != null
                        ? request.getSuccessUrl()
                        : successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(request.getCancelUrl() != null ? request.getCancelUrl() : cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.getCurrency())
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Car Rental Booking")
                                                                .setDescription(request.getDescription())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                );

        if (request.getCustomerEmail() != null) {
            builder.setCustomerEmail(request.getCustomerEmail());
        }

        builder.putMetadata("booking_reference", request.getBookingReference());

        return Session.create(builder.build());
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        if (isMockMode()) {
            Session mockSession = new Session();
            mockSession.setId(sessionId);
            mockSession.setPaymentStatus("paid");
            mockSession.setStatus("complete");
            return mockSession;
        }
        return Session.retrieve(sessionId);
    }

    public Event constructEvent(String payload, String sigHeader) throws StripeException {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid webhook signature: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing webhook: " + e.getMessage(), e);
        }
    }
}
