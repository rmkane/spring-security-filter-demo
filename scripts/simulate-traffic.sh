#!/usr/bin/env bash
#
# Simulate periodic probe traffic against a local Spring Boot app.
# Expects header-logging rules to suppress noise from probe user-agents.
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

# -----------------------------------------------------------------------------
# Configuration (override via environment)
# -----------------------------------------------------------------------------
readonly INTERVAL_SECONDS="${INTERVAL_SECONDS:-5}"

# -----------------------------------------------------------------------------
# Scenarios: each line is "description|path (relative to BASE_URL)|User-Agent"
# -----------------------------------------------------------------------------
readonly -a SCENARIOS=(
    "GET /actuator/health/liveness (HealthChecker/1.0)|/actuator/health/liveness|HealthChecker/1.0"
    "GET /actuator/health/readiness (HealthChecker/1.0)|/actuator/health/readiness|HealthChecker/1.0"
    "GET /api/default/info (ELB-HealthChecker/2.0)|/api/default/info|ELB-HealthChecker/2.0"
)

# -----------------------------------------------------------------------------
# Lifecycle
# -----------------------------------------------------------------------------
shutdown_gracefully() {
    printf '\n'
    log "Received SIGINT; exiting traffic simulator."
    exit 0
}

trap shutdown_gracefully INT

# -----------------------------------------------------------------------------
# Output
# -----------------------------------------------------------------------------
clear_screen() {
    printf '\033[2J\033[H'
}

print_cycle_banner() {
    clear_screen
    log "Traffic simulator — interval ${INTERVAL_SECONDS}s, base ${BASE_URL}"
    printf '\n'
}

print_result() {
    local description="$1"
    local http_code="$2"
    local label="OK"

    if [[ "$http_code" != "200" ]]; then
        label="ERROR"
    fi

    log "${description} -> HTTP ${http_code} (${label})"
}

# -----------------------------------------------------------------------------
# HTTP
# -----------------------------------------------------------------------------
# Prints HTTP status code only (stdout).
# API paths include mTLS forwarding headers when the app uses require-mtls-headers=true.
http_get_code() {
    local url="$1"
    local user_agent="$2"
    local path="$3"

    local -a curl_args=(
        -s -o /dev/null -w '%{http_code}' -X GET "$url"
        -H "user-agent: ${user_agent}"
        -H "accept: application/json"
    )
    if [[ "$path" == *"/api/"* ]]; then
        curl_args+=("${ACME_MTLS_HEADERS[@]}")
    fi
    curl "${curl_args[@]}"
}

run_scenario() {
    local description="$1"
    local path="$2"
    local user_agent="$3"
    local url="${BASE_URL}${path}"
    local code

    code="$(http_get_code "$url" "$user_agent" "$path")"
    print_result "$description" "$code"
}

run_all_scenarios() {
    local entry
    for entry in "${SCENARIOS[@]}"; do
        IFS='|' read -r description path user_agent <<<"$entry"
        run_scenario "$description" "$path" "$user_agent"
    done
}

# -----------------------------------------------------------------------------
# Main loop
# -----------------------------------------------------------------------------
main() {
    while true; do
        print_cycle_banner
        run_all_scenarios
        printf '\n'
        log "Sleeping ${INTERVAL_SECONDS}s before next cycle..."
        sleep "$INTERVAL_SECONDS"
    done
}

main "$@"
