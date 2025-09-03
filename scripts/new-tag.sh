#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <Version like V0.01> <Title> [YYMMDD]" >&2
  echo "Example: $0 V0.01 FirstDrop 250903" >&2
  exit 2
fi

ver="$1"; shift
title="$1"; shift
date="${1:-$(date +%y%m%d)}"

tag="RELEASE/${date}_${ver}_${title}"

if [[ ! "$tag" =~ ^RELEASE/[0-9]{6}_V[0-9]+\.[0-9]{2}_[A-Za-z0-9_-]+$ ]]; then
  echo "Invalid tag: $tag" >&2
  echo "Expected: RELEASE/YYMMDD_V0.01_Title" >&2
  exit 2
fi

git tag -a "$tag" -m "Release ${ver} (${date}): ${title}"
echo "Created annotated tag: $tag"
echo "Push with: git push origin '$tag'"

