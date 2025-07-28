package com.donorbox.backend.service;

import com.donorbox.backend.entity.Donation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("testing@alphaseam.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("testing@alphaseam.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendSubmissionStatusEmail(String email, String subject, String htmlContent) {
        try {
            sendHtmlEmail(email, subject, htmlContent);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    public void sendContactNotificationEmails(String contactName, String contactEmail, String contactPhone, String subject, String content, String orgEmail) {
        try {
            String formattedDate = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

            String orgSubject = "New Contact Form Submission: " + subject;
            String orgHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>New Contact Form Submission</h2>
                <p><strong>Name:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Phone:</strong> %s</p>
                <p><strong>Subject:</strong> %s</p>
                <p><strong>Submitted:</strong> %s</p>
                <p><strong>Message:</strong><br>%s</p>
                </div></body></html>
                """, contactName, contactEmail, contactPhone != null ? contactPhone : "Not provided", subject, formattedDate, content);

            String confirmationSubject = "Thank you for contacting us - We received your message";
            String confirmationHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>Thank You for Contacting Us!</h2>
                <p>Dear %s,</p>
                <p>We received your message regarding <strong>%s</strong> on %s. Our team will get back to you shortly.</p>
                <p><strong>Your Message:</strong><br>%s</p>
                <p>Thank you,<br>The DonorBox Team</p>
                </div></body></html>
                """, contactName, subject, formattedDate, content);

            sendHtmlEmail(orgEmail, orgSubject, orgHtml);
            sendHtmlEmail(contactEmail, confirmationSubject, confirmationHtml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendVolunteerNotificationEmails(String firstName, String lastName, String email, String phone, String skills, String availability, String experience, String motivation, String orgEmail) {
        try {
            String fullName = firstName + " " + lastName;
            String formattedDate = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

            String orgSubject = "New Volunteer Registration: " + fullName;
            String orgHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>New Volunteer Registration</h2>
                <p><strong>Name:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Phone:</strong> %s</p>
                <p><strong>Skills:</strong> %s</p>
                <p><strong>Availability:</strong> %s</p>
                <p><strong>Experience:</strong><br>%s</p>
                <p><strong>Motivation:</strong><br>%s</p>
                <p><strong>Registration Date:</strong> %s</p>
                </div></body></html>
                """, fullName, email, phone != null ? phone : "Not provided", skills != null ? skills : "Not provided", availability != null ? availability : "Not provided", experience != null ? experience : "Not provided", motivation != null ? motivation : "Not provided", formattedDate);

            String welcomeSubject = "Welcome to Our Volunteer Community - Registration Confirmed";
            String welcomeHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>Welcome to Our Volunteer Family!</h2>
                <p>Dear %s,</p>
                <p>Thank you for registering to volunteer with us. We're excited to have you on board!</p>
                <p><strong>Skills:</strong> %s</p>
                <p><strong>Availability:</strong> %s</p>
                <p><strong>Registered On:</strong> %s</p>
                <p>We'll get in touch with you soon regarding the next steps.</p>
                <p>Warm regards,<br>The DonorBox Team</p>
                </div></body></html>
                """, fullName, skills, availability, formattedDate);

            sendHtmlEmail(orgEmail, orgSubject, orgHtml);
            sendHtmlEmail(email, welcomeSubject, welcomeHtml);
        } catch (Exception e) {
            e.printStackTrace(); // Replace with logger in production
        }
    }

    public void sendDonationEmails(Donation donation, String orgEmail) {
        try {
            String causeName = donation.getCause() != null ? donation.getCause().getTitle() : "General Fund";
            String status = donation.getStatus() != null ? donation.getStatus().toString() : "UNKNOWN";

            String statusColor = switch (donation.getStatus()) {
                case COMPLETED -> "#28a745";
                case FAILED -> "#dc3545";
                case PENDING -> "#ffc107";
                case REFUNDED -> "#17a2b8";
                default -> "#6c757d";
            };

            String statusMessage = switch (donation.getStatus()) {
                case COMPLETED -> "We are delighted to confirm that your donation has been successfully received.";
                case PENDING -> "Your donation is currently pending. We will notify you once the payment is confirmed.";
                case FAILED -> "Unfortunately, your donation could not be processed. Please try again or contact support.";
                case REFUNDED -> "Your donation has been refunded. For further details, please contact support.";
                default -> "Your donation status is currently unknown. Please contact support for more information.";
            };

            String donorSubject = "Donation Status - " + status;
            String donorHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>Donation Status: %s</h2>
                <p>Dear %s,</p>
                <p>%s</p>
                <p><strong>Amount:</strong> %s %s</p>
                <p><strong>Cause:</strong> %s</p>
                <p><strong>Date:</strong> %s</p>
                <p><strong>Status:</strong> <span style='color:%s;'>%s</span></p>
                </div></body></html>
                """, status, donation.getDonorName(), statusMessage, donation.getCurrency(), donation.getAmount(), causeName, donation.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")), statusColor, status);

            String orgSubject = "New Donation - " + status;
            String orgHtml = String.format("""
                <html><body style='font-family: Arial;'>
                <div style='max-width: 600px; padding: 20px;'>
                <h2 style='color: #2c5aa0;'>Donation Received</h2>
                <p><strong>Donor Name:</strong> %s</p>
                <p><strong>Email:</strong> %s</p>
                <p><strong>Phone:</strong> %s</p>
                <p><strong>Amount:</strong> %s %s</p>
                <p><strong>Cause:</strong> %s</p>
                <p><strong>Date:</strong> %s</p>
                <p><strong>Status:</strong> <span style='color:%s;'>%s</span></p>
                </div></body></html>
                """, donation.getDonorName(), donation.getDonorEmail(), donation.getDonorPhone(), donation.getCurrency(), donation.getAmount(), causeName, donation.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")), statusColor, status);

            sendHtmlEmail(donation.getDonorEmail(), donorSubject, donorHtml);
            sendHtmlEmail(orgEmail, orgSubject, orgHtml);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
}
