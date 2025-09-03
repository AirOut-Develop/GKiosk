#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 dev|release <Title> [NN|Vx.yy]" >&2
  echo "Examples:" >&2
  echo "  $0 dev UIBaseLayout 00" >&2
  echo "  $0 release FirstDrop V0.01" >&2
  exit 2
fi

kind="$1"; shift
title="$1"; shift || true

date=$(date +%y%m%d)

case "$kind" in
  dev|DEV|develop|DEVELOP)
    nn="${1:-00}"
    name="DEVELOP/${date}_${nn}_${title}"
    ;;
  release|RELEASE)
    ver="${1:-V0.01}"
    name="RELEASE/${date}_${ver}_${title}"
    ;;
  *)
    echo "Unknown kind: $kind (use dev|release)" >&2
    exit 2
    ;;
esac

git checkout -b "$name"
echo "Created and switched to branch: $name"

