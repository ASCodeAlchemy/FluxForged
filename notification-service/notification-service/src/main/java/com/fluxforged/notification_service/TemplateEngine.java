package com.fluxforged.notification_service;

import org.springframework.stereotype.Component;

@Component("emailTemplateEngine")
public class TemplateEngine {
    public String getPipelineEmailTemplate(String projectName, String status, String runId, String details) {
        String statusColor = switch (status.toUpperCase()) {
            case "STARTED" -> "#2f81f7";
            case "SUCCESS" -> "#238636";
            case "FAILED" -> "#da3633";
            default -> "#8b949e";
        };

        return """
<div style="background-color:#f3f4f6; padding:40px 16px; font-family:'Segoe UI', Arial, sans-serif;">
    <div style="max-width:620px; margin:0 auto; background-color:#ffffff; border:1px solid #d0d7de; border-radius:14px; overflow:hidden; box-shadow:0 12px 30px rgba(0,0,0,0.08);">

        <!-- Header -->
        <div style="background:linear-gradient(135deg, #0d1117, #161b22); padding:28px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:24px; letter-spacing:1px;">
                FLUX<span style="color:#2f81f7;">FORGED</span>
            </h1>
            <p style="color:#8b949e; margin-top:6px; font-size:13px;">
                Continuous Integration & Deployment
            </p>
        </div>

        <!-- Body -->
        <div style="padding:36px 32px; color:#1f2328;">
            <h2 style="margin-top:0; font-size:20px;">
                Pipeline Status
                <span style="
                    display:inline-block;
                    margin-left:10px;
                    padding:6px 12px;
                    font-size:13px;
                    font-weight:600;
                    color:#ffffff;
                    background-color:%s;
                    border-radius:999px;">
                    %s
                </span>
            </h2>

            <div style="margin-top:24px; font-size:14px; line-height:1.6;">
                <p style="margin:6px 0;"><strong>Project:</strong> %s</p>
                <p style="margin:6px 0;"><strong>Run ID:</strong> #%s</p>
            </div>

            <hr style="border:none; border-top:1px solid #e5e7eb; margin:28px 0;">

            <h3 style="font-size:16px; margin-bottom:10px;">Execution Details</h3>
            <div style="
                background-color:#f6f8fa;
                border:1px solid #d0d7de;
                padding:16px;
                border-radius:8px;
                font-family:Consolas, 'Courier New', monospace;
                font-size:13px;
                color:#24292f;
                white-space:pre-wrap;">
                %s
            </div>
        </div>

        <!-- Footer -->
        <div style="background-color:#fafafa; padding:18px; text-align:center; font-size:12px; color:#6e7781;">
            <p style="margin:0;">
                This is an automated notification from <strong>FluxForged</strong>.
            </p>
            <p style="margin:6px 0 0;">
                © %d FluxForged · All rights reserved
            </p>
        </div>

    </div>
</div>
""".formatted(statusColor, status, projectName, runId, details, java.time.Year.now().getValue());

    }

    public String getRegisterTemplate(String code) {
        return getAuthTemplate("Verify your email", "Please use the code below to verify your FluxForged account.", code);
    }

    public String getLoginTemplate(String code) {
        return getAuthTemplate("Secure Sign-in", "Use this code to complete your login request.", code);
    }

    private String getAuthTemplate(String title, String message, String code) {
        return """
        <div style="background-color: #fafafa; padding: 50px 20px; font-family: Arial, sans-serif; text-align: center;">
            <div style="max-width: 450px; margin: auto; background: white; padding: 30px; border-radius: 12px; border: 1px solid #e1e4e8;">
                <h2 style="color: #0d1117;">%s</h2>
                <p style="color: #57606a;">%s</p>
                <div style="font-size: 32px; font-weight: bold; color: #2f81f7; letter-spacing: 5px; margin: 20px 0;">%s</div>
            </div>
        </div>
        """.formatted(title, message, code);
    }

    public String getWelcomeTemplate(String email) {
        String username = email.split("@")[0];
        return """
    <div style="background-color: #fafafa; padding: 50px 20px; font-family: sans-serif;">
        <div style="max-width: 550px; margin: auto; background: white; padding: 40px; border-radius: 12px; border: 1px solid #e1e4e8; text-align: center;">
            <div style="background-color: #0d1117; padding: 15px; border-radius: 8px; display: inline-block; margin-bottom: 20px;">
                <h2 style="color: #ffffff; margin: 0;">FLUX<span style="color: #2f81f7;">FORGED</span></h2>
            </div>
            <h1 style="color: #1f2328; font-size: 24px;">Registration Successful!</h1>
            <p style="color: #57606a; font-size: 16px; line-height: 1.6;">
                Hi <strong>%s</strong>, your account has been verified. 
                You are now ready to build and deploy your Java Spring Boot projects using our high-performance pipeline.
            </p>
            <div style="margin: 30px 0;">
                <a href="http://localhost:5173/login" style="background-color: #238636; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Get Started</a>
            </div>
            <p style="color: #8b949e; font-size: 12px; margin-top: 40px;">
                If you did not create this account, please ignore this email.
            </p>
        </div>
    </div>
    """.formatted(username);
    }
}
