#!/usr/bin/env bash
# Loads credentials from .env (which is git-ignored — see .env.example)
# and starts the app. Run `chmod +x run.sh` once if it's not executable.

set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f .env ]; then
    echo "No .env file found. Copy .env.example to .env and set a real"
    echo "username/password first:"
    echo ""
    echo "  cp .env.example .env"
    echo ""
    exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

if [ "${APP_PASSWORD:-}" = "changeme-too" ]; then
    echo "Warning: APP_PASSWORD in .env is still the placeholder. Set a real"
    echo "password before making this reachable outside your machine."
fi

mvn spring-boot:run
