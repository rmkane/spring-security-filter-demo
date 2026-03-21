<!-- omit in toc -->
# Spring Properties Demo

Small Spring Boot demo app with request/response header logging that can be enabled for debugging and filtered by request headers.

<!-- omit in toc -->
## Table of Contents

- [Run The App](#run-the-app)
- [Header Logging Filter](#header-logging-filter)
- [mTLS header authentication](#mtls-header-authentication)
- [Environment Variables](#environment-variables)
  - [`ACME_SECURITY_HEADERFILTER_DISABLED`](#acme_security_headerfilter_disabled)
  - [`ACME_SECURITY_HEADERFILTER_IGNOREHEADERS`](#acme_security_headerfilter_ignoreheaders)
  - [`LOG_ACME_SECURITY_LVL`](#log_acme_security_lvl)
- [Logging Behavior](#logging-behavior)
- [Rolling Logs](#rolling-logs)
- [Health Probes](#health-probes)
- [Local Verification](#local-verification)

## Run The App

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

Useful local scripts:

```bash
./scripts/endpoints/get-info.sh
./scripts/simulate-traffic.sh
```

## Header Logging Filter

The request/response header logging filter is controlled by `application.yml` under:

```yaml
acme:
  security:
    headers:
      subject-dn: x-amzn-mtls-clientcert-subject
      issuer-dn: x-amzn-mtls-clientcert-issuer
      require-mtls-headers: true
      allowed-client-subjects:
        - "CN=demo-client,O=local"
    header-filter:
      disabled: false
      ignore-headers: |-
        {
          "user-agent": ["ELB-HealthChecker/*", "HealthChecker/*"]
        }
```

The logger level is configured with:

```yaml
logging:
  level:
    org.acme.demo.security: ${LOG_ACME_SECURITY_LVL:DEBUG}
```

The filter logs when all of the following are true:

- `acme.security.header-filter.disabled` is `false` (default)
- the logger for `org.acme.demo.security` is at `DEBUG`
- the request does not match an ignored header rule

## mTLS header authentication

Load balancers (e.g. AWS ALB with mTLS) can forward client certificate metadata as HTTP headers. This app maps those into a Spring Security **`Principal`** (`MtlsClientPrincipal`: subject + issuer DNs) and a **`ROLE_MTLS_CLIENT`** authority—**no `UserDetailsService` / in-memory password users** are required; the “who may call the API” list is **`acme.security.headers.allowed-client-subjects`** (exact subject DN match, trimmed).

- **`/actuator/health` and `/actuator/health/**`** → **`permitAll`** (probes work **without** mTLS headers; the mTLS filter skips these entirely).
- At **`DEBUG`**, the mTLS filter’s **“Authenticated request as mTLS client…”** line is **omitted** when the request matches **`acme.security.header-filter.ignore-headers`** (e.g. `ELB-HealthChecker/*`, `HealthChecker/*`), so simulator/probe traffic doesn’t spam logs while real clients still log.
- **`/error`** → **`permitAll`** (error dispatch).
- **`DefaultController`** API methods also use **`@PreAuthorize`** (method security enabled via **`@EnableMethodSecurity`**): access when **`require-mtls-headers`** is **`false`**, or when the caller has **`hasRole("MTLS_CLIENT")`**—this mirrors the HTTP matcher rule for defense in depth.
- When **`acme.security.headers.require-mtls-headers`** is **`true`** (default in `application.yml`):
  - **`/api/**`** requires **`hasRole("MTLS_CLIENT")`**: valid mTLS headers **and** (if **`allowed-client-subjects`** is non-empty) a subject DN that appears in that list.
  - Missing/blank subject or issuer headers → **401 Unauthorized**.
  - Headers present but subject not allow-listed → **403 Forbidden**.
  - Any other path → **403** (`denyAll`).
- Set **`require-mtls-headers: false`** (e.g. in tests via `@TestPropertySource`) to open all routes for local experiments without forwarding headers.

HTTP Basic and the default Spring Boot generated user are **disabled**.

Scripts send demo mTLS headers on **API** calls via `scripts/lib/common.sh` (`MTLS_SUBJECT_DN`, `MTLS_ISSUER_DN`—defaults align with **`allowed-client-subjects`** in `application.yml`).

Use **`GET /api/default/whoami`** to verify the resolved principal when enforcement is enabled.

## Environment Variables

Defaults for `acme.security.header-filter` and `acme.security.headers` live in `application.yml`. In Kubernetes/Helm (or similar), use these names when you inject configuration (for example via `env` plus a templated `application.yaml`, `SPRING_APPLICATION_JSON`, or your platform’s config merge).

### `ACME_SECURITY_HEADERFILTER_DISABLED`

When `true`, turns off the header logging filter entirely. Use your deployment wiring to map this into `acme.security.header-filter.disabled` (for example `true` / `false` as a boolean).

Examples:

```bash
export ACME_SECURITY_HEADERFILTER_DISABLED=false
export ACME_SECURITY_HEADERFILTER_DISABLED=true
```

### `ACME_SECURITY_HEADERFILTER_IGNOREHEADERS`

Single-line JSON string: request header names mapped to values (exact or `*` wildcards) that suppress logging when matched. Map this into `acme.security.header-filter.ignore-headers` in your external config.

Example:

```bash
export ACME_SECURITY_HEADERFILTER_IGNOREHEADERS='{"user-agent":["ELB-HealthChecker/*","HealthChecker/*"]}'
```

Behavior notes:

- configured header names are normalized to lowercase before matching
- header values support exact matches and simple `*` wildcard patterns
- empty or missing JSON means no ignored-header rules
- trailing commas in the JSON are tolerated by the parser

### `LOG_ACME_SECURITY_LVL`

Overrides the Spring logging level for the `org.acme.demo.security` package.

Examples:

```bash
export LOG_ACME_SECURITY_LVL=DEBUG
export LOG_ACME_SECURITY_LVL=INFO
export LOG_ACME_SECURITY_LVL=OFF
```

## Logging Behavior

When enabled, the filter writes curl-style request and response headers to the application logs.

Example shape:

```text
> GET /api/default/info HTTP/1.1
> host: localhost:8080
> user-agent: curl/8.7.1

< HTTP/1.1 200
< Content-Type: application/json
```

Sensitive header values are masked for:

- `Authorization`
- `Proxy-Authorization`
- `Cookie`
- `Set-Cookie`
- `X-Api-Key`

## Rolling Logs

Logback is configured with a daily rolling file appender in `src/main/resources/logback-spring.xml`.

Default behavior:

- active log file: `logs/demo-api.log`
- rolled file pattern: `logs/demo-api.yyyy-MM-dd.log`
- retention: 30 days

You can override the output directory with:

```bash
export LOG_DIR=/path/to/logs
```

## Health Probes

Spring Boot health probes are enabled so these endpoints return `200`:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

## Local Verification

Start the app:

```bash
mvn spring-boot:run
```

Make a manual request that should appear in the logs:

```bash
./scripts/endpoints/get-info.sh
```

Run simulated probe traffic that should be ignored by the filter:

```bash
./scripts/simulate-traffic.sh
```

Both scripts share `scripts/lib/common.sh` (`SERVER_PORT`, `BASE_URL`, `log`). Override the target host/port with e.g. `BASE_URL=http://localhost:9090 ./scripts/endpoints/get-info.sh`.

When you finish production debugging, reduce the log level again to avoid excess log volume:

```bash
export LOG_ACME_SECURITY_LVL=INFO
```

Check the log output:

```bash
less logs/demo-api.log
```
