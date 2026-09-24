#!/usr/bin/env bash
# Verifies an APK is installable on current Android devices, including 16 KB page-size kernels:
#  - zip alignment of uncompressed native libraries (zipalign -P 16)
#  - ELF LOAD segment alignment >= 16 KB for every arm64-v8a / x86_64 library
#  - signature (apksigner), when the APK is signed
set -euo pipefail

apk="${1:?usage: check-apk.sh <apk> [--signed]}"
signed="${2:-}"
build_tools="$(ls -d "${ANDROID_HOME:?ANDROID_HOME not set}"/build-tools/* | sort -V | tail -1)"

echo "== zipalign (16 KB) using $build_tools"
"$build_tools/zipalign" -c -P 16 -v 4 "$apk" | tail -1

echo "== ELF segment alignment"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
unzip -q "$apk" 'lib/*' -d "$tmp" 2>/dev/null || true
fail=0
for so in "$tmp"/lib/arm64-v8a/*.so "$tmp"/lib/x86_64/*.so; do
  [ -e "$so" ] || continue
  # Smallest alignment across LOAD segments, e.g. 0x4000.
  align="$(readelf -lW "$so" | awk '$1=="LOAD"{print $NF}' | sort | head -1)"
  if (( align >= 0x4000 )); then
    echo "ok   $align  ${so#"$tmp"/}"
  else
    echo "FAIL $align  ${so#"$tmp"/}"
    fail=1
  fi
done

if [ "$signed" = "--signed" ]; then
  echo "== apksigner"
  # verify exits non-zero on any signature problem; the grep only trims the output
  # (signer labels differ between build-tools versions, so it must not decide the result).
  certs="$("$build_tools/apksigner" verify --print-certs "$apk")"
  grep -E "certificate (DN|SHA-256 digest)" <<<"$certs" || echo "$certs"
fi

exit "$fail"
