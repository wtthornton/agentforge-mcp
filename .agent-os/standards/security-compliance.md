# Security & Compliance Standard (Enhanced)

## MANDATORY: Cursor Agent Management

**CRITICAL**: All security and compliance work requires fresh AI agents for optimal validation and protection.

### Agent Assignment for Security Tasks
- **@static-analyzer**: Security validation, compliance checking, vulnerability detection, standards enforcement
- **@infrastructure-agent**: Infrastructure security, container security, deployment security, monitoring security
- **@backend-agent**: Backend security, API security, authentication, authorization, data protection
- **@frontend-agent**: Frontend security, UI security, input validation, XSS prevention, CSRF protection
- **@database-agent**: Database security, SQL injection prevention, data encryption, access control

### Security Workflow
1. **Clear Context**: Press `Ctrl+Shift+C` before security work
2. **New Conversation**: Press `Ctrl+Shift+N` for fresh agent
3. **Select Agent**: Choose appropriate agent type for security domain
4. **Security Analysis**: Use agent expertise for comprehensive security validation
5. **Vulnerability Assessment**: Identify and address security issues with agent guidance
6. **Compliance Verification**: Verify security compliance with agent assistance

## Secure Defaults & Hardening
- Deny-by-default; TLS and headers enabled by default.
- Run containers as non-root; drop unneeded Linux capabilities.

## Dependency Management (SCA)
- Use OWASP Dep‑Check, Snyk, Dependabot; maintain SBOMs.

## Container & Infra Security
- Minimal base images (alpine/distroless), Trivy scans in CI.
- Scan IaC for misconfigurations (Checkov, AWS Config).

## Secret Management
- No secrets in code; use Vault, AWS Secrets Manager, or GitHub Secrets.
- Rotate secrets regularly; enable leak detection.

## OWASP Top 10 Compliance
- Robust authz; parameterized queries; proper crypto.
- Logging & alerting for suspicious activity.

## Security Lessons Learned
- **Capture:** Document security incidents, vulnerabilities, and mitigation strategies
- **Apply:** Update security standards based on lessons learned
- **Reference:** See `@~/.agent-os/lessons-learned/categories/operations/README.md` for security lessons

**Cursor Effect:** Avoids insecure code and **generates DevSecOps-ready pipelines and configs**.
