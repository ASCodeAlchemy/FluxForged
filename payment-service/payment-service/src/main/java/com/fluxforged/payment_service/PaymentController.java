package com.fluxforged.payment_service;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final RazorpayClient razorpayClient;
    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestHeader("X-User-Email") String email, @RequestParam Memberships plan) throws RazorpayException {


        double amount = (plan == Memberships.PRO) ? 500.0 : 2000.0;

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", amount * 100); // Amount in paise
        paymentLinkRequest.put("currency", "INR");
        paymentLinkRequest.put("description", "FluxForged " + plan + " Subscription");

        JSONObject customer = new JSONObject();
        customer.put("email", email);
        paymentLinkRequest.put("customer", customer);

        paymentLinkRequest.put("callback_url", "http://localhost:8085/api/payments/verify");
        paymentLinkRequest.put("callback_method", "get");

        PaymentLink payment = razorpayClient.paymentLink.create(paymentLinkRequest);

        return ResponseEntity.ok(Map.of(
                "payment_url", payment.get("short_url"),
                "order_id", payment.get("id")
        ));
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_payment_link_id") String linkId,
            @RequestParam("razorpay_payment_link_status") String status) {

        if ("paid".equals(status)) {
            try {
                PaymentLink paymentLink = razorpayClient.paymentLink.fetch(linkId);
                JSONObject linkJson = paymentLink.toJson();

                String email = linkJson.getJSONObject("customer").getString("email");

                String planFromRazorpay = linkJson.getString("description").toUpperCase();

                Subscription sub = subscriptionRepository.findByUserEmail(email);
                if (sub == null) {
                    sub = new Subscription();
                    sub.setUserEmail(email);
                }
                sub.setStatus(Status.ACTIVE);


                Memberships planType = planFromRazorpay.contains("ENTERPRISE") ? Memberships.ENTERPRISE : Memberships.PRO;
                sub.setPlanType(planType);
                subscriptionRepository.save(sub);


                Map<String, String> event = new HashMap<>();
                event.put("type", "PAYMENT_SUCCESS");
                event.put("email", email);
                event.put("plan", planType.name());

                kafkaTemplate.send("notification-events", email, event);
                kafkaTemplate.send("membership-updates", email, event);

                return ResponseEntity.ok("<h1>Payment Received!</h1><p>Plan " + planType + " activated for " + email + "</p>");

            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error during verification: " + e.getMessage());
            }
        }
        return ResponseEntity.badRequest().body("Payment failed.");
    }

    @GetMapping("/is-subscribed")
    public ResponseEntity<Boolean> isUserSubscribed(@RequestParam("email") String email) {
        Subscription sub = subscriptionRepository.findByUserEmail(email);

        boolean isActive = (sub != null && sub.getStatus() == Status.ACTIVE);

        return ResponseEntity.ok(isActive);
    }
}
