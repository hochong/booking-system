#!/usr/bin/env bash
# One-shot dependency install for every part of the booking system: Node.js
# itself (if missing), npm packages for the frontend, and Maven dependencies
# for both Spring Boot services.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
source scripts/common.sh

ensure_node() {
  if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then # check if node js and npm are installed
    echo "==> Node.js already installed: $(node -v)"
    return
  fi

  local os
  os="$(detect_os)"
  echo "==> Node.js not found - installing (detected OS: $os)"

  case "$os" in
    windows)
      # if windows, use winget to install Node.js LTS. If winget is not available, instruct the user to install Node.js manually.
      if ! command -v winget >/dev/null 2>&1; then
        echo "winget not found. Install Node.js LTS manually from https://nodejs.org/ and re-run this script." >&2
        exit 1
      fi
      # --silent suppresses the installer's own UI screens. It can NOT suppress the
      # Windows UAC elevation prompt itself (that's an OS security dialog, not part of
      # the installer) - if this hangs, check for a hidden "Allow this app..." prompt
      # (Alt+Tab / other monitors), or re-run this script from an Administrator shell
      # to avoid the prompt entirely.
      winget install --id OpenJS.NodeJS.LTS -e --silent --accept-package-agreements --accept-source-agreements
      # winget updates the machine-wide PATH, but this shell won't pick that up until
      # it restarts - add the default install dir so the rest of this script can still
      # find node/npm without requiring a new terminal.
      export PATH="/c/Program Files/nodejs:$PATH"
      ;;
    macos)
      if ! command -v brew >/dev/null 2>&1; then
        echo "Homebrew not found. Install Node.js LTS manually from https://nodejs.org/ and re-run this script." >&2
        exit 1
      fi
      brew install node
      ;;
    linux)
      # if linux, use apt-get to install Node.js LTS.
      if command -v apt-get >/dev/null 2>&1; then
        sudo apt-get update && sudo apt-get install -y nodejs npm
      else
        echo "No supported package manager found. Install Node.js LTS manually from https://nodejs.org/ and re-run this script." >&2
        exit 1
      fi
      ;;
    *)
      echo "Unrecognized OS. Install Node.js LTS manually from https://nodejs.org/ and re-run this script." >&2
      exit 1
      ;;
  esac

  if ! command -v node >/dev/null 2>&1; then
    echo "Node.js was installed but isn't on PATH in this shell. Open a new terminal and re-run this script." >&2
    exit 1
  fi
  echo "==> Node.js installed: $(node -v)"
}

ensure_node

echo "==> Installing booking-api dependencies"
mvn -f services/booking-api/pom.xml -q dependency:go-offline

echo "==> Installing booking-processor dependencies"
mvn -f services/booking-processor/pom.xml -q dependency:go-offline

echo "==> Installing notification-service dependencies"
mvn -f services/notification-service/pom.xml -q dependency:go-offline

echo "==> Installing frontend dependencies"
npm install --prefix frontend

echo "==> Done. Run ./run.sh to start everything."
