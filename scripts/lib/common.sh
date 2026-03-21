#!/usr/bin/env bash
#
# scripts/lib/common.sh — shared defaults and logging (source from other scripts; do not run directly).
#
# Environment:
#   SERVER_PORT  default 8080
#   BASE_URL     default http://localhost:${SERVER_PORT}

SERVER_PORT="${SERVER_PORT:-8080}"
BASE_URL="${BASE_URL:-http://localhost:${SERVER_PORT}}"

# mTLS forwarding header names (keep in sync with acme.security.headers in application.yml)
MTLS_SUBJECT_HEADER="${MTLS_SUBJECT_HEADER:-x-amzn-mtls-clientcert-subject}"
MTLS_ISSUER_HEADER="${MTLS_ISSUER_HEADER:-x-amzn-mtls-clientcert-issuer}"
# Demo DNs when require-mtls-headers=true (override in env for your org)
MTLS_SUBJECT_DN="${MTLS_SUBJECT_DN:-CN=demo-client,O=local}"
MTLS_ISSUER_DN="${MTLS_ISSUER_DN:-CN=demo-ca,O=local}"

# Extra curl -H args for API calls (health probes do not need these)
ACME_MTLS_HEADERS=(
    -H "${MTLS_SUBJECT_HEADER}: ${MTLS_SUBJECT_DN}"
    -H "${MTLS_ISSUER_HEADER}: ${MTLS_ISSUER_DN}"
)

timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

log() {
    printf '[%s] %s\n' "$(timestamp)" "$*"
}
