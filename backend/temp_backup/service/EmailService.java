package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.from-name}")
    private String fromName;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    // Order-related emails
    
    public void sendOrderConfirmation(String userEmail, Order order) {
        log.info("Sending order confirmation email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            
            String htmlContent = buildOrderConfirmationEmail(order);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Order confirmation email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send order confirmation email", e);
        }
    }
    
    public void sendOrderStatusUpdate(String userEmail, Order order) {
        log.info("Sending order status update email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Order Status Update - " + order.getOrderNumber());
            
            String htmlContent = buildOrderStatusUpdateEmail(order);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Order status update email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send order status update email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send order status update email", e);
        }
    }
    
    public void sendOrderCancellation(String userEmail, Order order, String reason) {
        log.info("Sending order cancellation email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Order Cancelled - " + order.getOrderNumber());
            
            String htmlContent = buildOrderCancellationEmail(order, reason);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Order cancellation email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send order cancellation email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send order cancellation email", e);
        }
    }
    
    public void sendOrderTracking(String userEmail, Order order) {
        log.info("Sending order tracking email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Your Order is Shipped - " + order.getOrderNumber());
            
            String htmlContent = buildOrderTrackingEmail(order);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Order tracking email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send order tracking email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send order tracking email", e);
        }
    }
    
    // User-related emails
    
    public void sendWelcomeEmail(String userEmail, String firstName) {
        log.info("Sending welcome email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Welcome to Our Store!");
            
            String htmlContent = buildWelcomeEmail(firstName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
    
    public void sendPasswordResetEmail(String userEmail, String resetToken) {
        log.info("Sending password reset email to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Password Reset Request");
            
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
            String htmlContent = buildPasswordResetEmail(resetUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", userEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    public void sendEmailVerification(String userEmail, String verificationToken) {
        log.info("Sending email verification to: {}", userEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Verify Your Email Address");
            
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            String htmlContent = buildEmailVerificationEmail(verificationUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email verification sent successfully to: {}", userEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send email verification to: {}", userEmail, e);
            throw new RuntimeException("Failed to send email verification", e);
        }
    }
    
    // Notification emails
    
    public void sendLowStockNotification(String adminEmail, String productName, int currentStock) {
        log.info("Sending low stock notification to: {}", adminEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("Low Stock Alert - " + productName);
            
            String htmlContent = buildLowStockNotificationEmail(productName, currentStock);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Low stock notification sent successfully to: {}", adminEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send low stock notification to: {}", adminEmail, e);
            throw new RuntimeException("Failed to send low stock notification", e);
        }
    }
    
    public void sendNewReviewNotification(String adminEmail, String productName, String reviewerName, int rating) {
        log.info("Sending new review notification to: {}", adminEmail);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("New Review for " + productName);
            
            String htmlContent = buildNewReviewNotificationEmail(productName, reviewerName, rating);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("New review notification sent successfully to: {}", adminEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send new review notification to: {}", adminEmail, e);
            throw new RuntimeException("Failed to send new review notification", e);
        }
    }
    
    // Email template builders
    
    private String buildOrderConfirmationEmail(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Order Confirmation</h2>");
        html.append("<p>Thank you for your order! Your order has been confirmed and is being processed.</p>");
        html.append("<h3>Order Details:</h3>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ").append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("</p>");
        html.append("<p><strong>Total Amount:</strong> $").append(String.format("%.2f", order.getTotalAmount())).append("</p>");
        
        html.append("<h3>Order Items:</h3>");
        html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        html.append("<tr><th>Product</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");
        
        for (Order.OrderItem item : order.getItems()) {
            html.append("<tr>");
            html.append("<td>").append(item.getProductName()).append("</td>");
            html.append("<td>").append(item.getQuantity()).append("</td>");
            html.append("<td>$").append(String.format("%.2f", item.getPrice())).append("</td>");
            html.append("<td>$").append(String.format("%.2f", item.getPrice() * item.getQuantity())).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("<p>You can track your order status by visiting our website.</p>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildOrderStatusUpdateEmail(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Order Status Update</h2>");
        html.append("<p>Your order status has been updated.</p>");
        html.append("<h3>Order Details:</h3>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Current Status:</strong> ").append(order.getStatus()).append("</p>");
        html.append("<p><strong>Payment Status:</strong> ").append(order.getPaymentStatus()).append("</p>");
        
        if (order.getTrackingNumber() != null) {
            html.append("<p><strong>Tracking Number:</strong> ").append(order.getTrackingNumber()).append("</p>");
        }
        
        html.append("<p>You can track your order status by visiting our website.</p>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildOrderCancellationEmail(Order order, String reason) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Order Cancelled</h2>");
        html.append("<p>Your order has been cancelled.</p>");
        html.append("<h3>Order Details:</h3>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Cancellation Reason:</strong> ").append(reason).append("</p>");
        html.append("<p><strong>Refund Amount:</strong> $").append(String.format("%.2f", order.getTotalAmount())).append("</p>");
        html.append("<p>Your refund will be processed within 3-5 business days.</p>");
        html.append("<p>If you have any questions, please contact our customer service.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildOrderTrackingEmail(Order order) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Your Order is Shipped!</h2>");
        html.append("<p>Great news! Your order has been shipped and is on its way to you.</p>");
        html.append("<h3>Shipping Details:</h3>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Tracking Number:</strong> ").append(order.getTrackingNumber()).append("</p>");
        html.append("<p><strong>Carrier:</strong> ").append(order.getCarrier()).append("</p>");
        html.append("<p>You can track your package using the tracking number above.</p>");
        html.append("<p>Thank you for shopping with us!</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildWelcomeEmail(String firstName) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Welcome to Our Store!</h2>");
        html.append("<p>Hello ").append(firstName).append(",</p>");
        html.append("<p>Thank you for joining our store! We're excited to have you as a customer.</p>");
        html.append("<p>Here's what you can do:</p>");
        html.append("<ul>");
        html.append("<li>Browse our wide selection of products</li>");
        html.append("<li>Create wishlists and save your favorite items</li>");
        html.append("<li>Track your orders and manage your account</li>");
        html.append("<li>Leave reviews and ratings for products</li>");
        html.append("</ul>");
        html.append("<p>If you have any questions, feel free to contact our customer service team.</p>");
        html.append("<p>Happy shopping!</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildPasswordResetEmail(String resetUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Password Reset Request</h2>");
        html.append("<p>You have requested to reset your password.</p>");
        html.append("<p>Click the link below to reset your password:</p>");
        html.append("<p><a href='").append(resetUrl).append("'>Reset Password</a></p>");
        html.append("<p>This link will expire in 24 hours.</p>");
        html.append("<p>If you didn't request this password reset, please ignore this email.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildEmailVerificationEmail(String verificationUrl) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Verify Your Email Address</h2>");
        html.append("<p>Thank you for registering with our store!</p>");
        html.append("<p>Please click the link below to verify your email address:</p>");
        html.append("<p><a href='").append(verificationUrl).append("'>Verify Email</a></p>");
        html.append("<p>This link will expire in 24 hours.</p>");
        html.append("<p>If you didn't create an account, please ignore this email.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildLowStockNotificationEmail(String productName, int currentStock) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Low Stock Alert</h2>");
        html.append("<p>Product: <strong>").append(productName).append("</strong></p>");
        html.append("<p>Current Stock: <strong>").append(currentStock).append("</strong></p>");
        html.append("<p>Please consider restocking this product soon.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String buildNewReviewNotificationEmail(String productName, String reviewerName, int rating) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>New Review Received</h2>");
        html.append("<p>Product: <strong>").append(productName).append("</strong></p>");
        html.append("<p>Reviewer: <strong>").append(reviewerName).append("</strong></p>");
        html.append("<p>Rating: <strong>").append(rating).append("/5 stars</strong></p>");
        html.append("<p>Please review and moderate this review in the admin panel.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }
}
