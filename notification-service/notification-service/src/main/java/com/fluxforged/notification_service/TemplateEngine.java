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

    public String getAuthTemplate(String title, String message, String code) {
        return """
<div style="background-color:#f4f6f8; padding:60px 20px; font-family:'Segoe UI', Helvetica, Arial, sans-serif;">
    <div style="max-width:520px; margin:0 auto; background-color:#ffffff; border-radius:14px;
                border:1px solid #d8dee4; box-shadow:0 12px 30px rgba(0,0,0,0.08); overflow:hidden;">
        <div style="background:linear-gradient(135deg, #0d1117, #161b22); padding:32px; text-align:center;">
            <h1 style="margin:0; font-size:26px; font-weight:700; color:#ffffff; letter-spacing:-0.5px;">
                FLUX<span style="color:#2f81f7;">FORGED</span>
            </h1>
            <p style="margin:8px 0 0; font-size:13px; color:#8b949e;">Secure Cloud-Native Platform</p>
        </div>
        <div style="padding:40px 34px; text-align:center;">
            <h2 style="margin:0 0 12px; font-size:22px; font-weight:600; color:#1f2328;">%s</h2>
            <p style="margin:0 0 28px; font-size:15px; line-height:1.7; color:#57606a;">%s</p>
            <div style="background-color:#f6f8fa; border:1px solid #d0d7de; border-radius:10px;
                        padding:22px 28px; display:inline-block; margin:10px auto 20px;">
                <span style="font-family:'Courier New', monospace; font-size:36px; font-weight:700;
                             letter-spacing:10px; color:#2f81f7;">%s</span>
            </div>
            <p style="margin:20px 0 0; font-size:13px; color:#8c959f;">
                This verification code will expire in <strong style="color:#cf222e;">10 minutes</strong>.
            </p>
        </div>
        <div style="background-color:#f6f8fa; border-top:1px solid #e1e4e8; padding:22px; text-align:center;">
            <p style="margin:0; font-size:12px; color:#57606a; line-height:1.5;">
                © 2026 FluxForged Cloud-Native Systems
            </p>
        </div>
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

    public String getPaymentReceiptTemplate(String email) {
        String username = email.split("@")[0];
        return """
<div style="background-color:#f3f4f6; padding:40px 16px; font-family:'Segoe UI', Arial, sans-serif;">
    <div style="max-width:550px; margin:0 auto; background-color:#ffffff; border:1px solid #d0d7de; border-radius:14px; overflow:hidden;">
        
        <div style="background:#0d1117; padding:25px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:22px;">
                FLUX<span style="color:#2f81f7;">FORGED</span>
            </h1>
        </div>

        <div style="padding:30px; text-align:center;">
            <div style="background-color:#2f81f715; color:#2f81f7; display:inline-block; padding:6px 16px; border-radius:20px; font-weight:bold; font-size:12px; margin-bottom:15px;">
                PRO MEMBERSHIP ACTIVATED
            </div>
            <h2 style="margin:0; color:#1f2328;">Payment Successful</h2>
            <p style="color:#57606a; margin-top:10px;">Hi <strong>%s</strong>, your account has been upgraded.</p>
            
            <div style="background-color:#f6f8fa; border:1px solid #e1e4e8; border-radius:8px; margin:25px 0; padding:20px; text-align:left;">
                <p style="margin:5px 0; font-size:14px;"><strong>Plan:</strong> FluxForged Pro (Monthly)</p>
                <p style="margin:5px 0; font-size:14px;"><strong>Amount Paid:</strong> ₹500.00</p>
                <p style="margin:5px 0; font-size:14px;"><strong>Status:</strong> Active</p>
            </div>

            <ul style="text-align:left; color:#1f2328; font-size:14px; line-height:1.8;">
                <li>🚀 Unlimited Pipeline Runs</li>
                <li>⚡ Priority Build Queue</li>
                <li>📦 10GB MinIO Artifact Storage</li>
            </ul>

            <div style="margin-top:30px;">
                <a href="http://localhost:5173/dashboard" style="background-color:#2f81f7; color:white; padding:12px 25px; text-decoration:none; border-radius:6px; font-weight:bold; display:inline-block;">Go to Dashboard</a>
            </div>
        </div>

        <div style="background-color:#fafafa; padding:15px; text-align:center; font-size:11px; color:#8b949e; border-top:1px solid #e1e4e8;">
            © %d FluxForged · Secure Payment via Razorpay
        </div>
    </div>
</div>
""".formatted(username, java.time.Year.now().getValue());
    }

    public String getEnterpriseReceiptTemplate(String email) {
        String username = email.split("@")[0];
        return """
<div style="background-color:#f3f4f6; padding:40px 16px; font-family:'Segoe UI', Arial, sans-serif;">
    <div style="max-width:550px; margin:0 auto; background-color:#ffffff; border:1px solid #d0d7de; border-radius:14px; overflow:hidden;">
        
        <div style="background:#0d1117; padding:25px; text-align:center;">
            <h1 style="color:#ffffff; margin:0; font-size:22px;">
                FLUX<span style="color:#f59e0b;">FORGED</span> <span style="font-size:12px; vertical-align:middle; color:#f59e0b; border:1px solid #f59e0b; padding:2px 6px; border-radius:4px; margin-left:5px;">ENTERPRISE</span>
            </h1>
        </div>

        <div style="padding:30px; text-align:center;">
            <h2 style="margin:0; color:#1f2328;">Welcome to the Elite Tier</h2>
            <p style="color:#57606a; margin-top:10px;">Hi <strong>%s</strong>, your enterprise workspace is ready.</p>
            
            <div style="background-color:#fffbeb; border:1px solid #fef3c7; border-radius:8px; margin:25px 0; padding:20px; text-align:left;">
                <p style="margin:5px 0; font-size:14px; color:#92400e;"><strong>Tier:</strong> Enterprise Dedicated</p>
                <p style="margin:5px 0; font-size:14px; color:#92400e;"><strong>Support:</strong> 24/7 Priority Concierge</p>
                <p style="margin:5px 0; font-size:14px; color:#92400e;"><strong>Status:</strong> Active</p>
            </div>

            <ul style="text-align:left; color:#1f2328; font-size:14px; line-height:1.8;">
                <li>💎 <strong>Dedicated Build Nodes</strong> (No Waiting)</li>
                <li>🛡️ <strong>Advanced Security Scanning</strong></li>
                <li>🤝 <strong>Unlimited Team Members</strong></li>
                <li>📈 <strong>Custom Analytics Dashboard</strong></li>
            </ul>

            <div style="margin-top:30px;">
                <a href="http://localhost:5173/dashboard" style="background-color:#0d1117; color:white; padding:12px 25px; text-decoration:none; border-radius:6px; font-weight:bold; display:inline-block;">Launch Enterprise Console</a>
            </div>
        </div>

        <div style="background-color:#fafafa; padding:15px; text-align:center; font-size:11px; color:#8b949e; border-top:1px solid #e1e4e8;">
            © %d FluxForged · Enterprise Division
        </div>
    </div>
</div>
""".formatted(username, java.time.Year.now().getValue());
    }
}
