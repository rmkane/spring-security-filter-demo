#!/usr/bin/env bash
#
# Manual GET with a normal curl user-agent — should appear in header debug logs
# (unlike automated probe traffic from simulate-traffic.sh).
#
# Usage: ./scripts/endpoints/get-info.sh [path]
#   path defaults to /api/default/info
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../lib/common.sh
source "${SCRIPT_DIR}/../lib/common.sh"

readonly ENDPOINT="${1:-/api/default/info}"

log "Manual request: GET ${BASE_URL}${ENDPOINT}"

curl -sS -X GET "${BASE_URL}${ENDPOINT}" \
    "${ACME_MTLS_HEADERS[@]}" \
    -H "accept: application/json" \
    -H "user-agent: curl/8.7.1"
printf '\n'
