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
        <div style="background-color: #fafafa; padding: 50px 20px; font-family: 'Segoe UI', Arial, sans-serif;">
            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #e1e4e8; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1);">
                <div style="background-color: #0d1117; padding: 25px; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 22px;">FLUX<span style="color: #2f81f7;">FORGED</span></h1>
                </div>
                <div style="padding: 40px 30px;">
                    <h2 style="color: #1f2328;">Pipeline Status: <span style="color: %s;">%s</span></h2>
                    <p><strong>Project:</strong> %s</p>
                    <p><strong>Run ID:</strong> #%s</p>
                    <div style="background: #f6f8fa; padding: 15px; border-radius: 6px; font-family: monospace;">
                        %s
                    </div>
                </div>
            </div>
        </div>
        """.formatted(statusColor, status, projectName, runId, details);
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
}
