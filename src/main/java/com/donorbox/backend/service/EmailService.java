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
            message.setFrom("info.sairuraldevelopmenttrust@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace(); // Replace with logger in production
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("info.sairuraldevelopmenttrust@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
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

            String donorSubject = switch (donation.getStatus()) {
                case COMPLETED -> "Your Donation is Complete - Thank You!";
                case PENDING -> "Your Donation is Pending - Action Required?";
                case FAILED -> "Action Required: Your Donation Failed";
                case REFUNDED -> "Your Donation Has Been Refunded";
                default -> "Update on Your Donation";
            };

            String donorHeading = switch (donation.getStatus()) {
                case COMPLETED -> "Donation Confirmed - Thank You!";
                case PENDING -> "Your Donation is Pending";
                case FAILED -> "Donation Unsuccessful";
                case REFUNDED -> "Donation Refunded";
                default -> "Update on Your Donation";
            };

            String orgSubject = switch (donation.getStatus()) {
                case COMPLETED -> "New Donation: " + donation.getCurrency() + " " + donation.getAmount() + " Received!";
                case PENDING -> "Pending Donation: " + donation.getCurrency() + " " + donation.getAmount() + " from " + donation.getDonorName();
                case FAILED -> "Failed Donation: " + donation.getCurrency() + " " + donation.getAmount() + " from " + donation.getDonorName();
                case REFUNDED -> "Donation Refunded: " + donation.getCurrency() + " " + donation.getAmount() + " (ID: " + donation.getId() + ")";
                default -> "Donation Status Update (ID: " + donation.getId() + ")";
            };

            String orgHeading = switch (donation.getStatus()) {
                case COMPLETED -> "New Donation Received!";
                case PENDING -> "New Pending Donation";
                case FAILED -> "Donation Attempt Failed";
                case REFUNDED -> "Donation Refunded";
                default -> "Donation Status Update";
            };

            String formattedDate = donation.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

            // Donor HTML Email
            String donorHtml = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                    <h1 style='color: #2c5aa0; text-align: center;'>%s</h1>
                    <p>Dear %s,</p>
                    <p>%s</p>
                    <div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>
                        <h3 style='margin-top: 0; color: #2c5aa0;'>Donation Details:</h3>
                        <p><strong>Amount:</strong> %s %s</p>
                        <p><strong>Cause:</strong> %s</p>
                        <p><strong>Donation ID:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Phone:</strong> %s</p>
                        <p><strong>Status:</strong> <span style='color: %s;'>%s</span></p>
                    </div>
                    <p>Your generous contribution will make a real difference in supporting our cause. 
                    We will keep you updated on how your donation is being used.</p>
                    <p>With heartfelt gratitude,<br>The DonorBox Team</p>
                </div>
                </body>
                </html>
                """,
                donorHeading,
                donation.getDonorName(),
                statusMessage,
                donation.getCurrency(), donation.getAmount(),
                causeName,
                donation.getId(),
                formattedDate,
                donation.getDonorPhone() != null ? donation.getDonorPhone() : "Not provided",
                statusColor,
                status
            );

            // Organization HTML Email
            String orgHtml = String.format("""
                <html>
                <body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                    <h1 style='color: #2c5aa0; text-align: center;'>%s</h1>
                    <p>A new donation has been processed through the DonorBox platform with the following status:</p>
                    <div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>
                        <h3 style='margin-top: 0; color: #2c5aa0;'>Donation Details:</h3>
                        <p><strong>Donor Name:</strong> %s</p>
                        <p><strong>Donor Email:</strong> %s</p>
                        <p><strong>Donor Phone:</strong> %s</p>
                        <p><strong>Amount:</strong> %s %s</p>
                        <p><strong>Cause:</strong> %s</p>
                        <p><strong>Donation ID:</strong> %s</p>
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Status:</strong> <span style='color: %s;'>%s</span></p>
                        <p><strong>Message:</strong> %s</p>
                    </div>
                    <p>Please log into the admin dashboard to view more details and manage this donation.</p>
                    <p>Best regards,<br>DonorBox System</p>
                </div>
                </body>
                </html>
                """,
                orgHeading,
                donation.getDonorName(),
                donation.getDonorEmail(),
                donation.getDonorPhone() != null ? donation.getDonorPhone() : "Not provided",
                donation.getCurrency(), donation.getAmount(),
                causeName,
                donation.getId(),
                formattedDate,
                statusColor,
                status,
                donation.getMessage() != null ? donation.getMessage() : "No message provided"
            );

            // Send both emails
            sendHtmlEmail(donation.getDonorEmail(), donorSubject, donorHtml);
            sendHtmlEmail(orgEmail, orgSubject, orgHtml);

        } catch (Exception e) {
            e.printStackTrace(); // Replace with logger
        }
    }
}
