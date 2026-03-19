<!-- omit in toc -->
# Spring Properties Demo

Small Spring Boot demo app with request/response header logging that can be enabled for debugging and filtered by request headers.

<!-- omit in toc -->
## Table of Contents

- [Run The App](#run-the-app)
- [Header Logging Filter](#header-logging-filter)
- [Environment Variables](#environment-variables)
  - [`ACME_SECURITY_HEADER_FILTER_ENABLED`](#acme_security_header_filter_enabled)
  - [`ACME_SECURITY_HEADER_FILTER_IGNORED_HEADERS`](#acme_security_header_filter_ignored_headers)
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
    header-filter:
      enabled: ${ACME_SECURITY_HEADER_FILTER_ENABLED:true}
      ignored-headers: '${ACME_SECURITY_HEADER_FILTER_IGNORED_HEADERS:{"user-agent":["GLB-Client/1.35+","HealthChecker/1.0"]}}'
```

The logger level is configured with:

```yaml
logging:
  level:
    org.acme.demo.security: ${LOG_ACME_SECURITY_LVL:DEBUG}
```

The filter logs when all of the following are true:

- `acme.security.header-filter.enabled` is `true`
- the logger for `org.acme.demo.security` is at `DEBUG`
- the request does not match an ignored header rule

## Environment Variables

### `ACME_SECURITY_HEADER_FILTER_ENABLED`

Enables or disables the filter entirely.

Examples:

```bash
export ACME_SECURITY_HEADER_FILTER_ENABLED=true
export ACME_SECURITY_HEADER_FILTER_ENABLED=false
```

### `ACME_SECURITY_HEADER_FILTER_IGNORED_HEADERS`

JSON string containing request header names mapped to exact values that should be ignored.

Example:

```bash
export ACME_SECURITY_HEADER_FILTER_IGNORED_HEADERS='{"user-agent":["GLB-Client/1.35+","HealthChecker/1.0"]}'
```

Behavior notes:

- header names are matched using the exact configured key as read from the request
- header values are exact-match only
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

Check the log output:

```bash
less logs/demo-api.log
```
