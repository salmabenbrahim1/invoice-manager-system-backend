package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailValidationService emailValidationService;

    public boolean sendEmail(String toEmail, String subject, String body) {
        boolean isValid = emailValidationService.isEmailValid(toEmail);

        if (!isValid) {
            System.err.println("Adresse email invalide : " + toEmail);
            return false;
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            helper.setText(body, true);

            javaMailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            return false;
        }
    }




}