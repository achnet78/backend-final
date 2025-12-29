package com.example.Portfolio.contact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendComplaintConfirmation(String toEmail, String userName, String lang) {
        try {
            String displayName = (userName == null || userName.trim().isEmpty())
                    ? ("en".equalsIgnoreCase(lang) ? "Customer" : "عميل")
                    : userName;

            String subject;
            String body;

            if ("en".equalsIgnoreCase(lang)) {
                subject = "Your message has been received";
                body = String.format(
                        "Hello %s,\n\n" +
                                "Thank you for your message. I've received it and will review the details carefully before responding as soon as possible.\n\n" +
                                "Warm regards,\n\n" +
                                "Ashraf Almuhtaseb",
                        displayName
                );
            } else {
                subject = "تم استلام رسالتك بنجاح";
                body = String.format(
                        "مرحبًا %s،\n\n" +
                                "شكرًا لتواصلك. تم استلام رسالتك، وسأقوم بمراجعتها بعناية والرد عليك في أقرب وقت ممكن.\n\n" +
                                "أطيب الأمنيات،\n\n" +
                                "أشرف المحتسب",
                        displayName
                );
            }

            sendEmailViaResend(toEmail, subject, body);
            log.info("Confirmation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send confirmation email to: {}", toEmail, e);
            throw new RuntimeException("فشل إرسال الإيميل: " + e.getMessage(), e);
        }
    }

    public void sendComplaintToReceiverEmail(String userEmail, String userName, String msg, String lang) {
        try {
            String subject;
            String body;

            if ("en".equalsIgnoreCase(lang)) {
                subject = "New message from your website – " + (userName == null ? "User" : userName);
                body = String.format(
                        "Sender: %s <%s>\n\nMessage:\n%s",
                        userName == null ? "No name" : userName,
                        userEmail == null ? "No email" : userEmail,
                        msg == null ? "" : msg
                );
            } else {
                subject = "رسالة جديدة من موقعك – " + (userName == null ? "مستخدم" : userName);
                body = String.format(
                        "المرسل: %s <%s>\n\nالرسالة:\n%s",
                        userName == null ? "لا يوجد اسم" : userName,
                        userEmail == null ? "لا يوجد بريد" : userEmail,
                        msg == null ? "" : msg
                );
            }

            sendEmailViaResend("Ashraf@iData.center", subject, body);
            log.info("Notification email sent successfully to website owner");

        } catch (Exception e) {
            log.error("Failed to send notification email to website owner", e);
            throw new RuntimeException("فشل إرسال الإيميل لمستقبل الشكاوى: " + e.getMessage(), e);
        }
    }

    private void sendEmailViaResend(String to, String subject, String bodyText) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", fromEmail);
        body.put("to", new String[]{to});
        body.put("subject", subject);
        body.put("text", bodyText);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully via Resend API");
            } else {
                log.error("Resend API returned error: {}", response.getBody());
                throw new RuntimeException("Resend API error: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to send email via Resend", e);
            throw new RuntimeException("Failed to send email via Resend: " + e.getMessage(), e);
        }
    }
}