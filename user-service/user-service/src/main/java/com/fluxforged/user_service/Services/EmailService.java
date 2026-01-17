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
<div style="background-color:#f4f6f8; padding:60px 20px; font-family:'Segoe UI', Helvetica, Arial, sans-serif;">
    <div style="max-width:520px; margin:0 auto; background-color:#ffffff; border-radius:14px;
                border:1px solid #d8dee4; box-shadow:0 12px 30px rgba(0,0,0,0.08); overflow:hidden;">

        <!-- Header -->
        <div style="background:linear-gradient(135deg, #0d1117, #161b22); padding:32px; text-align:center;">
            <h1 style="margin:0; font-size:26px; font-weight:700; color:#ffffff; letter-spacing:-0.5px;">
                FLUX<span style="color:#2f81f7;">FORGED</span>
            </h1>
            <p style="margin:8px 0 0; font-size:13px; color:#8b949e;">
                Secure Cloud-Native Platform
            </p>
        </div>

        <!-- Body -->
        <div style="padding:40px 34px; text-align:center;">
            <h2 style="margin:0 0 12px; font-size:22px; font-weight:600; color:#1f2328;">
                Email Verification Required
            </h2>

            <p style="margin:0 0 28px; font-size:15px; line-height:1.7; color:#57606a;">
                Thank you for choosing <strong>FluxForged</strong>.
                Please confirm your email address by entering the verification code below.
                This step helps us keep your account secure.
            </p>

            <!-- Verification Code -->
            <div style="background-color:#f6f8fa; border:1px solid #d0d7de; border-radius:10px;
                        padding:22px 28px; display:inline-block; margin:10px auto 20px;">
                <span style="font-family:'Courier New', monospace; font-size:36px; font-weight:700;
                             letter-spacing:10px; color:#2f81f7;">
                    %s
                </span>
            </div>

            <p style="margin:20px 0 0; font-size:13px; color:#8c959f;">
                This verification code will expire in
                <strong style="color:#cf222e;">10 minutes</strong>.
            </p>

            <p style="margin:26px 0 0; font-size:13px; color:#8c959f;">
                If you did not request this email, you can safely ignore it.
            </p>
        </div>

        <!-- Footer -->
        <div style="background-color:#f6f8fa; border-top:1px solid #e1e4e8; padding:22px; text-align:center;">
            <p style="margin:0; font-size:12px; color:#57606a; line-height:1.5;">
                © 2026 FluxForged Cloud-Native Systems<br>
                This message was sent because an account was registered using this email address.
            </p>
        </div>

    </div>
</div>
""".formatted(code);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
