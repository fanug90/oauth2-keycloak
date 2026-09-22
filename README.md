# Spring Security OAuth2 with Keycloak

A Spring Boot demonstration application that implements OpenID Connect login with Keycloak.

The application uses Spring Security OAuth2 Client to authenticate users through Keycloak and applies role-based authorization to separate user, manager, and administrator dashboards.

## Features

- Login with Keycloak using OAuth2/OpenID Connect
- Custom OAuth2 login page
- User profile display
- Keycloak role extraction from the access token
- Role-based access control
- Separate dashboards for users, managers, and administrators
- OIDC client-initiated logout
- Thymeleaf server-side rendered views
- Spring Security integration with Keycloak

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring Security OAuth2 Client
- Keycloak
- Thymeleaf
- Maven
- Tailwind CSS browser CDN

## Project Structure

```text
src/
├── main/
│   ├── java/com/spring_security_demo/oauth2_keycloak/
│   │   ├── Oauth2KeycloakApplication.java
│   │   ├── config/
│   │   │   ├── AppConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   └── LoginController.java
│   │   └── service/
│   │       └── CustomOidcUserService.java
│   └── resources/
│       ├── application.yml
│       └── templates/
│           ├── admin.html
│           ├── home.html
│           ├── login.html
│           ├── manager.html
│           └── user.html
└── test/
    └── java/
        └── .../OauthAppApplicationTests.java
```

## How Authentication Works

1. The user opens the application.
2. Spring Security redirects unauthenticated users to `/login`.
3. The login page provides a **Sign in with Keycloak** link.
4. The user is redirected to Keycloak through:

   ```text
   /oauth2/authorization/keycloak
   ```

5. Keycloak authenticates the user and redirects back to:

   ```text
   /login/oauth2/code/keycloak
   ```

6. `CustomOidcUserService` loads the OIDC user information.
7. Keycloak client roles are read from the access token.
8. Roles are converted into Spring Security authorities such as:

   ```text
   ROLE_USER
   ROLE_MANAGER
   ROLE_ADMIN
   ```

9. After successful authentication, the user is redirected to `/home`.

## Required Keycloak Configuration

Create or configure the following objects in Keycloak:

| Setting | Value |
|---|---|
| Realm | `demo-realm` |
| Client ID | `demo-app` |
| Client type | OpenID Connect |
| Grant type | Authorization Code |
| Redirect URI | `http://localhost:8080/login/oauth2/code/keycloak` |
| Issuer URI | `http://localhost:8081/realms/demo-realm` |

The application expects Keycloak to be available at:

```text
http://localhost:8081
```

Create users and assign them one or more of the following client roles:

- `user`
- `manager`
- `admin`

The role names are case-insensitive when they are converted into Spring Security authorities.

## Running Keycloak with Docker

Start Keycloak locally with Docker:

```bash
docker run --name keycloak \
  -p 8081:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

Open the Keycloak administration console:

```text
http://localhost:8081
```

Sign in using:

```text
Username: admin
Password: admin
```

Then create:

1. The `demo-realm` realm
2. The `demo-app` OpenID Connect client
3. The redirect URI:
   `http://localhost:8080/login/oauth2/code/keycloak`
4. The client roles:
   `user`, `manager`, and `admin`
5. At least one user with a password and an assigned role

> Keycloak configuration screens can differ between versions. Verify the client settings and redirect URI if the login callback fails.

## Application Configuration

The OAuth2 client is configured in `src/main/resources/application.yml`.

Do not commit client secrets to source control. Configure the secret through an environment variable instead:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: ${KEYCLOAK_CLIENT_ID:demo-app}
            client-secret: ${KEYCLOAK_CLIENT_SECRET}
            scope:
              - openid
              - email
              - profile
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/keycloak
        provider:
          keycloak:
            issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8081/realms/demo-realm}
            user-name-attribute: preferred_username
```

Set the required environment variables before starting the application:

```bash
export KEYCLOAK_CLIENT_ID=demo-app
export KEYCLOAK_CLIENT_SECRET=your-client-secret
export KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/demo-realm
```

On Windows PowerShell:

```powershell
$env:KEYCLOAK_CLIENT_ID="demo-app"
$env:KEYCLOAK_CLIENT_SECRET="your-client-secret"
$env:KEYCLOAK_ISSUER_URI="http://localhost:8081/realms/demo-realm"
```

## Run the Application

Make sure Keycloak is running, then start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Open the application:

```text
http://localhost:8080/login
```

Select **Sign in with Keycloak** and authenticate with a Keycloak user.

## Available Routes

| Route | Required role | Description |
|---|---|---|
| `/login` | Public | Custom login page |
| `/home` | Authenticated user | Displays the authenticated user's profile |
| `/user` | `USER` | User dashboard |
| `/manager` | `MANAGER` | Manager dashboard |
| `/admin` | `ADMIN` | Administrator dashboard |
| `/logout` | Authenticated user | Logs out through the OIDC provider |

Unauthenticated requests are redirected to the login page. A user without the required role is denied access by Spring Security.

## Role Mapping

Keycloak roles are read from the access token under:

```json
{
  "resource_access": {
    "demo-app": {
      "roles": [
        "user",
        "manager",
        "admin"
      ]
    }
  }
}
```

`CustomOidcUserService` converts these roles into Spring Security authorities:

```text
user     -> ROLE_USER
manager  -> ROLE_MANAGER
admin    -> ROLE_ADMIN
```

The role names must match the role checks configured in `SecurityConfig`.

## Run Tests

Run the test suite with:

```bash
./mvnw test
```

The current test verifies that the Spring application context can load successfully.

## Troubleshooting

### Redirect URI mismatch

Verify that the Keycloak client contains this exact redirect URI:

```text
http://localhost:8080/login/oauth2/code/keycloak
```

### Keycloak connection error

Confirm that Keycloak is running and that the issuer URL is reachable:

```text
http://localhost:8081/realms/demo-realm
```

### Access denied for a valid user

Verify that:

- The user has the required client role.
- The role is assigned under the `demo-app` client.
- The role is located in the `resource_access.demo-app.roles` claim.
- The role name is `user`, `manager`, or `admin`.

### Login works but roles are missing

Inspect the access token and confirm that it contains the `resource_access` claim for the `demo-app` client.

## License

This project is a learning demonstration of Spring Security OAuth2, OpenID Connect, and Keycloak integration.
