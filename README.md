# AuthApp

A Spring Boot demo application showcasing **dual authentication**: traditional username/password login backed by a local MySQL database, alongside **Single Sign-On (SSO) via Gluu using OpenID Connect (OIDC/OAuth2)**. Users are routed to role-based dashboards after login.

## Tech Stack

- Java 17
- Spring Boot 4.0.2
  - Spring Web MVC
  - Spring Security (form login + OAuth2 Client)
  - Spring Data JPA
  - Thymeleaf (+ `thymeleaf-extras-springsecurity6` for security-aware templates)
- MySQL
- Gluu (OIDC Identity Provider) for SSO
- Lombok
- Maven

## Features

- **Local authentication** — username/password login against a local MySQL `users` table, with passwords hashed using BCrypt.
- **Gluu OIDC login** — federated login via a Gluu server, using the standard OAuth2 Authorization Code flow (authorize, token, JWKS, and userinfo endpoints).
- **Role-based access control** — users have a `role` field (`Admin` / normal user); `/admin-dashboard` is restricted to Admins, `/user-dashboard` is available to any authenticated user (local or Gluu-authenticated).
- **Unified login page** — a single `/` login page offers both the local login form and the "Login with Gluu" SSO option.
- **Logout handling** — clears the session and redirects back to the login page.

## Project Structure

```
demo-Auth_application/
├── .idea/
├── demo/                                  # Spring Boot (Maven) project — module name: AuthApp
│   ├── src/main/java/com/example/demo/
│   │   ├── config/
│   │   │   └── SecurityConfig.java        # Local + OIDC security filter chain
│   │   ├── model/
│   │   │   └── User.java                  # User entity (username, email, password, role)
│   │   └── service/
│   │       └── UserService.java
│   └── src/main/resources/
│       ├── application.properties         # DB + Gluu OAuth2/OIDC config
│       └── templates/
│           ├── login.html
│           ├── admin-dashboard.html
│           └── user-dashboard.html
└── README.md
```

## Getting Started

### Prerequisites

- Java 17 (JDK)
- Maven
- MySQL Server
- A Gluu server instance (or access to one) registered as an OIDC client, if you want to test SSO login

### Setup

1. Navigate to the project:
   ```bash
   cd demo
   ```

2. **Create the database:**
   ```sql
   CREATE DATABASE database_name;
   ```

3. **Configure `src/main/resources/application.properties`:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/database_name
   spring.datasource.username=mysql_username
   spring.datasource.password=mysql_password

   spring.security.oauth2.client.registration.gluu.client-id=client_id
   spring.security.oauth2.client.registration.gluu.client-secret=client_secret
   spring.security.oauth2.client.registration.gluu.redirect-uri=http://localhost:8080/login/oauth2/code/gluu

   spring.security.oauth2.client.provider.gluu.issuer-uri=https://your-gluu-server.example.com
   ```
   Replace the placeholder values with your own MySQL credentials and your Gluu server's client ID/secret and endpoints. Hibernate will create/update the schema automatically (`ddl-auto=update`).

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   The app will be available at `http://localhost:8080`.

### Usage

- Visit `/` to see the login page, with options for local login or Gluu SSO.
- Local users are authenticated against the `users` table (BCrypt-hashed passwords).
- After a successful login, users with the `Admin` role are redirected to `/admin-dashboard`; all other authenticated users go to `/user-dashboard`.

## Setting Up Gluu (for SSO login)

The app's `gluu` OAuth2 registration expects a running Gluu server that exposes standard OIDC endpoints (`issuer-uri`, authorize, token, JWKS, userinfo). Gluu's current product is **Gluu Flex** (built on the open-source Janssen Project). There are two practical ways to get one running:

### Option A — Quick local/dev instance (Docker All-In-One)

Good for testing this app locally. Requires 8 GB RAM / 4 CPU / 20 GB disk.

1. Download and run the installer script:
   ```bash
   wget https://raw.githubusercontent.com/GluuFederation/flex/main/automation/start_flex_aio_demo.sh
   chmod u+x start_flex_aio_demo.sh
   sudo bash start_flex_aio_demo.sh <your-fqdn> MYSQL "" <your-vm-public-ip>
   ```
   Replace `<your-fqdn>` (e.g. `demo.example.com`) and `<your-vm-public-ip>` with your own values. Choose `MYSQL` or `PGSQL` for the persistence backend.
2. Add an entry to `/etc/hosts` (or your DNS) mapping `<your-vm-public-ip>` to `<your-fqdn>`, so the hostname resolves.
3. Wait for the script to report the server is ready (a few minutes), then confirm you can reach `https://<your-fqdn>/.well-known/openid-configuration`.

> Gluu Flex requires a Software Statement Assertion (SSA) — a license JWT obtained free for a 30-day trial (or via a paid license) from **Agama Lab** (Gluu's licensing portal): sign in, go to **Market → Flex → Create New SSA**, and save the resulting JWT to a file to supply during installation.

### Option B — Production deployment

For anything beyond local testing, use the Helm-based deployment (Amazon/Google/Azure/Rancher/local Kubernetes) or the VM packages for Ubuntu/SUSE/Red Hat, both documented at [docs.gluu.org](https://docs.gluu.org). These follow the same three-step pattern: install the package, run initial setup (hostname, database, certs), then finish configuration through the Gluu Admin UI.

### Registering this app as an OIDC client in Gluu

Once your Gluu server is up, register a client so this app can authenticate against it:

1. Log in to the **Gluu Admin UI** for your instance.
2. Go to **Clients → Add Client** and configure:
   - **Client Name:** something recognizable, e.g. `demo-auth-application`
   - **Grant Types:** `authorization_code`
   - **Response Types:** `code`
   - **Redirect URIs:** `http://localhost:8080/login/oauth2/code/gluu` (must match `spring.security.oauth2.client.registration.gluu.redirect-uri` exactly)
   - **Scopes:** `openid`, `profile`, `email`, `phone`
   - **Token Endpoint Auth Method:** `client_secret_basic` (or `client_secret_post`, matching your Spring config)
3. Save the client and copy the generated **Client ID** and **Client Secret**.
4. Find your server's discovery document at `https://<your-fqdn>/.well-known/openid-configuration` to confirm the authorize, token, JWKS, and userinfo endpoint paths.
5. Update `application.properties` with these values:
   ```properties
   spring.security.oauth2.client.registration.gluu.client-id=<client-id-from-step-3>
   spring.security.oauth2.client.registration.gluu.client-secret=<client-secret-from-step-3>
   spring.security.oauth2.client.registration.gluu.scope=openid,profile,email,phone
   spring.security.oauth2.client.registration.gluu.authorization-grant-type=authorization_code
   spring.security.oauth2.client.registration.gluu.redirect-uri=http://localhost:8080/login/oauth2/code/gluu

   spring.security.oauth2.client.provider.gluu.issuer-uri=https://<your-fqdn>
   spring.security.oauth2.client.provider.gluu.authorization-uri=https://<your-fqdn>/oxauth/restv1/authorize
   spring.security.oauth2.client.provider.gluu.token-uri=https://<your-fqdn>/oxauth/restv1/token
   spring.security.oauth2.client.provider.gluu.jwk-set-uri=https://<your-fqdn>/oxauth/restv1/jwks
   spring.security.oauth2.client.provider.gluu.user-info-uri=https://<your-fqdn>/oxauth/restv1/userinfo
   spring.security.oauth2.client.provider.gluu.user-name-attribute=sub
   ```
6. Restart the app and click the "Login with Gluu" option on `/` to test the flow. On first login via Gluu, make sure your `UserService`/registration logic provisions a matching local `User` record (with a `role`) so the role-based redirect to `/admin-dashboard` or `/user-dashboard` works correctly.

## Security Notes

- Replace all placeholder credentials (`client_id`, `client_secret`, database credentials) in `application.properties` before committing or deploying — do not commit real secrets to version control.
- CSRF protection is enabled globally, with an explicit exemption for `/login`.
