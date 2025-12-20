package cz.petrf.prani.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class ThymeleafEmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    public void sendTokenEmail(String to, String userName,
                                 String activationLink, int expirationHours) {
        
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("activationLink", activationLink);
        context.setVariable("expirationHours", expirationHours);
        
        String htmlContent = templateEngine.process("email/welcome-email", context);
        
        sendHtmlEmail(to, "Vítejte v naší aplikaci!", htmlContent);
    }
    
    public void sendMagicLinkEmail(String to, String magicLink) {
        Context context = new Context();
        context.setVariable("magicLink", magicLink);
        context.setVariable("expirationMinutes", 15);
        
        String htmlContent = templateEngine.process("email/magic-link-email", context);
        
        sendHtmlEmail(to, "Přihlašovací odkaz", htmlContent);
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@vasedomena.cz");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
          throw new RuntimeException(e);
        }
    }
}