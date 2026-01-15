package com.fluxforged.user_service.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
 private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your Verification Code");


        String htmlContent = """
    <div style="background-color: #fafafa; padding: 50px 20px; font-family: 'Segoe UI', Helvetica, Arial, sans-serif;">
        <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05);">
            
            <div style="background-color: #0d1117; padding: 30px; text-align: center;">
                <h1 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px;">
                    FLUX<span style="color: #2f81f7;">FORGED</span>
                </h1>
            </div>

            <div style="padding: 40px 30px; text-align: center;">
                <h2 style="color: #1f2328; margin-top: 0; font-size: 20px; font-weight: 600;">Verify your email address</h2>
                <p style="color: #57606a; font-size: 15px; line-height: 1.6; margin-bottom: 30px;">
                    To complete your setup on <strong>FluxForged</strong>, please use the security code below. This ensures your account stays protected.
                </p>

                <div style="background-color: #f6f8fa; border: 1px solid #d0d7de; border-radius: 8px; 
                            font-size: 36px; font-family: 'Courier New', monospace; font-weight: 700; 
                            color: #0d1117; padding: 20px; margin: 20px auto; width: fit-content; 
                            letter-spacing: 8px; color: #2f81f7;">
                    %s
                </div>

                <p style="color: #8c959f; font-size: 13px; margin-top: 30px;">
                    This verification code will expire in <span style="color: #cf222e; font-weight: 600;">10 minutes</span>.
                </p>
            </div>

            <div style="background-color: #f6f8fa; padding: 20px; text-align: center; border-top: 1px solid #e1e4e8;">
                <p style="color: #57606a; font-size: 12px; margin: 0;">
                    &copy; 2026 FluxForged Cloud-Native Systems.<br>
                    You received this because an account was registered with this email.
                </p>
            </div>
        </div>
    </div>
""".formatted(code);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
