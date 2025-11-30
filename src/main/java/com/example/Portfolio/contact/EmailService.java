package com.example.Portfolio.contact;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String appFrom;

    public void sendComplaintConfirmation(String toEmail, String userName, String lang) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(appFrom);

            String subject;
            String body;

            // اسم للعرض لو المستخدم ما دخلش اسمه
            String displayName = (userName == null || userName.trim().isEmpty())
                    ? ("en".equalsIgnoreCase(lang) ? "Customer" : "عميل")
                    : userName;

            if ("en".equalsIgnoreCase(lang)) {
                subject = "Your message has been received";
                body = String.format(
                        "Hello %s,\n\n" +
                                "Thank you for your message. I’ve received it and will review the details carefully before responding as soon as possible.\n\n" +
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

            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("فشل إرسال الإيميل: " + e.getMessage(), e);
        }
    }

    public void sendComplaintToReceiverEmail(String userEmail, String userName, String msg, String lang) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("Ashraf@iData.center");
            helper.setFrom(appFrom);

            String subject;
            String body;

            if ("en".equalsIgnoreCase(lang)) {
                subject = "New message from your website — " + (userName == null ? "User" : userName);
                body = String.format(
                        "Sender: %s <%s>\n\nMessage:\n%s",
                        userName == null ? "No name" : userName,
                        userEmail == null ? "No email" : userEmail,
                        msg == null ? "" : msg
                );
            } else {
                subject = "رسالة جديدة من موقعك — " + (userName == null ? "مستخدم" : userName);
                body = String.format(
                        "المرسل: %s <%s>\n\nالرسالة:\n%s",
                        userName == null ? "لا يوجد اسم" : userName,
                        userEmail == null ? "لا يوجد بريد" : userEmail,
                        msg == null ? "" : msg
                );
            }

            helper.setSubject(subject);
            helper.setText(body, false);

            // reply-to لو المستخدم كتب إيميله
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                helper.setReplyTo(userEmail);
            } else {
                helper.setReplyTo(appFrom);
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("فشل إرسال الإيميل لمستقبل الشكاوى: " + e.getMessage(), e);
        }
    }
}
