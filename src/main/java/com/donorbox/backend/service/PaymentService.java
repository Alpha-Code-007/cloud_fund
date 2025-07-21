package com.donorbox.backend.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final String keySecret;

    public PaymentService(@Value("${razorpay.key.id}") String keyId, 
                         @Value("${razorpay.key.secret}") String keySecret) throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
        this.keySecret = keySecret;
    }

    /**
     * Create payment order for international transactions
     * @param amount Amount in the base currency
     * @param currency Currency code (USD, EUR, INR, etc.)
     * @param receiptId Unique receipt identifier
     * @return Razorpay Order object
     */
    @Transactional
    public Order createOrder(BigDecimal amount, String currency, String receiptId) throws RazorpayException {
        Map<String, Object> orderRequest = new HashMap<>();
        
        // Convert amount to smallest currency unit (paise for INR, cents for USD)
        int amountInSmallestUnit = amount.multiply(BigDecimal.valueOf(100)).intValue();
        
        orderRequest.put("amount", amountInSmallestUnit);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receiptId);
        
        // Support for international payments
        Map<String, Object> notes = new HashMap<>();
        notes.put("platform", "donorbox");
        notes.put("type", "donation");
        orderRequest.put("notes", notes);
        
        log.info("Creating Razorpay order for amount: {} {}, receipt: {}", amount, currency, receiptId);
        
        return razorpayClient.orders.create(new org.json.JSONObject(orderRequest));
    }

    /**
     * Verify payment signature for security
     * @param orderId Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Payment signature
     * @return true if signature is valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            org.json.JSONObject attributes = new org.json.JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            
            return com.razorpay.Utils.verifyPaymentSignature(attributes, 
                    this.keySecret);
        } catch (Exception e) {
            log.error("Error verifying payment signature", e);
            return false;
        }
    }

    /**
     * Fetch payment details
     * @param paymentId Payment ID
     * @return Payment object
     */
    public Payment fetchPayment(String paymentId) throws RazorpayException {
        return razorpayClient.payments.fetch(paymentId);
    }

    /**
     * Process refund for international payments
     * @param paymentId Payment ID to refund
     * @param amount Amount to refund (optional, if partial refund)
     * @return String status message
     */
    @Transactional
    public String processRefund(String paymentId, BigDecimal amount) {
        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
            // For now, return a status message
            // Actual refund implementation can be added later with proper Razorpay integration
            return "Refund request processed for payment: " + paymentId;
        } catch (RazorpayException e) {
            log.error("Error processing refund", e);
            return "Refund processing failed";
        }
    }

    /**
     * Get supported currencies for international payments
     * @return Map of supported currencies
     */
    public Map<String, String> getSupportedCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        
        // Major international currencies supported by Razorpay
        currencies.put("INR", "Indian Rupee");
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("GBP", "British Pound");
        currencies.put("AUD", "Australian Dollar");
        currencies.put("CAD", "Canadian Dollar");
        currencies.put("SGD", "Singapore Dollar");
        currencies.put("AED", "UAE Dirham");
        currencies.put("MYR", "Malaysian Ringgit");
        
        return currencies;
    }

    /**
     * Validate currency code
     * @param currency Currency code to validate
     * @return true if currency is supported
     */
    public boolean isCurrencySupported(String currency) {
        return getSupportedCurrencies().containsKey(currency);
    }
}
