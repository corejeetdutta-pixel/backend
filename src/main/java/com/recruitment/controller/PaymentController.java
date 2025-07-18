package com.recruitment.controller;

import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient("YOUR_KEY_ID", "YOUR_SECRET");

        JSONObject options = new JSONObject();
        options.put("amount", data.get("amount")); // e.g. 50000 for ₹500
        options.put("currency", "INR");
        options.put("receipt", "txn_" + UUID.randomUUID());

        Order order = razorpay.orders.create(options);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", order.get("id"));

        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) throws SignatureException {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        String secret = "YOUR_SECRET"; // Same as used during client setup

        String payload = orderId + "|" + paymentId;

        String actualSignature = hmacSha256(payload, secret);

        if (actualSignature.equals(signature)) {
            return ResponseEntity.ok().body(Map.of("status", "success"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "failed"));
        }
    }

    private String hmacSha256(String data, String key) throws SignatureException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal(data.getBytes());
            return Hex.encodeHexString(hmacData);
        } catch (Exception e) {
            throw new SignatureException("Failed to calculate HMAC", e);
        }
    }

}

