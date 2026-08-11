# MOA Backend Codex Guide

## Project
- Spring Boot backend packaged as a WAR and run as an executable application.
- Java 17, Maven, MyBatis, MySQL, JWT, OAuth, payments, push, and email integrations.
- Production service: `moa-backend.service` on the public MOA API host.

## Local checks
- Test and package: `.\\mvnw.cmd -B clean verify`
- Do not skip tests for a production deployment.

## Deployment
- Pushes and pull requests to `main` or `dev` run CI.
- A successful push to `main` uploads the verified WAR and restarts the production systemd service.
- Keep server environment values in `/etc/moa/moa-backend.env`; never commit them.
- Deployment must retain a rollback artifact and verify the restarted HTTP service.

## Safety
- Preserve unrelated user changes.
- Never print or commit credentials, tokens, private keys, OAuth secrets, payment secrets, or user data.
- Verify frontend, API, authentication entry points, service status, and logs before declaring deployment complete.
