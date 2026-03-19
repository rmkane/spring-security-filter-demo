#!/usr/bin/env bash

print_header() {
    printf '\033[2J\033[H'
    printf 'Simulating traffic every 5 seconds\n'
    printf 'Cycle started: %s\n\n' "$(date '+%Y-%m-%d %H:%M:%S')"
}

print_request() {
    local request_name="$1"
    local status_code="$2"
    local status_label="OK"

    if [[ "$status_code" != "200" ]]; then
        status_label="ERROR"
    fi

    printf '[%s] %s -> HTTP %s (%s)\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$request_name" "$status_code" "$status_label"
}

make_request() {
    local request_name="$1"
    local url="$2"
    local user_agent="$3"
    local status_code

    status_code="$(curl -s -o /dev/null -w '%{http_code}' -X GET "$url" \
        -H "user-agent: $user_agent" \
        -H "accept: application/json")"

    print_request "$request_name" "$status_code"
}

while true; do
    print_header

    # Simulate traffic from a health checker
    make_request "GET /actuator/health (HealthChecker/1.0)" "http://localhost:8080/actuator/health" "HealthChecker/1.0"
    make_request "GET /actuator/health/liveness (HealthChecker/1.0)" "http://localhost:8080/actuator/health/liveness" "HealthChecker/1.0"
    make_request "GET /actuator/health/readiness (HealthChecker/1.0)" "http://localhost:8080/actuator/health/readiness" "HealthChecker/1.0"

    # Simulate traffic from a GLB client against a real application endpoint
    make_request "GET /api/default/info (GLB-Client/1.35+)" "http://localhost:8080/api/default/info" "GLB-Client/1.35+"

    printf '\nSleeping 5 seconds before next cycle...\n'

    sleep 5
done