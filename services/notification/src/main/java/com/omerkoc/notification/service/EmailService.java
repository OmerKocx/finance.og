package com.omerkoc.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * E-posta gönderme işlemlerini gerçekleştiren servis sınıfı.
 * Spring Boot'un JavaMailSender bileşenini kullanarak e-posta gönderimi sağlar.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    // E-posta gönderimini gerçekleştirmek üzere Spring Boot tarafından otomatik
    // yapılandırılan mail gönderici nesne
    private final JavaMailSender mailSender;

    /**
     * Belirtilen alıcıya basit metin (simple text) formatında e-posta gönderir.
     *
     * @param to      E-postanın gönderileceği alıcı adresi (e-mail)
     * @param subject E-postanın konusu (başlığı)
     * @param body    E-postanın içeriği (mesaj metni)
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to {} with subject: {}", to, subject);
        try {
            // Basit metin tabanlı e-posta mesajı oluşturuluyor
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to); // Alıcı adresi set ediliyor
            message.setSubject(subject); // Konu başlığı set ediliyor
            message.setText(body); // E-posta içeriği set ediliyor

            // E-postayı gönder
            mailSender.send(message);
            log.info("Email successfully sent to {}", to);
        } catch (Exception e) {
            // E-posta gönderimi başarısız olursa loglanır fakat uygulamanın akışını
            // kesmemesi için hata yutulur
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }
}
