#!/usr/bin/env bash
# Shared helpers sourced by install.sh and run.sh.

detect_os() {
  case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) echo "windows" ;;
    Darwin*) echo "macos" ;;
    Linux*) echo "linux" ;;
    *) echo "unknown" ;;
  esac
}
