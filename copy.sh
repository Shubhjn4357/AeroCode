#!/usr/bin/env bash
# ==============================================================================
# AeroCode APK Copy & Export Script
# Automatically copies compiled APK files from build folders to /output and ./output
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="${SCRIPT_DIR}"

# Target output directories (both /output and workspace ./output for download access)
TARGET_DIR_WORKSPACE="${WORKSPACE_ROOT}/output"
TARGET_DIR_ROOT="/output"

echo "[AeroCode] Initializing APK auto-export..."
echo "[AeroCode] Workspace directory: ${WORKSPACE_ROOT}"

# Ensure output directories exist
mkdir -p "${TARGET_DIR_WORKSPACE}"
mkdir -p "${TARGET_DIR_ROOT}" 2>/dev/null || true

# Potential source directories for APK artifacts
CANDIDATE_PATHS=(
    "${WORKSPACE_ROOT}/.build-outputs"
    "${WORKSPACE_ROOT}/app/build/outputs/apk/debug"
    "${WORKSPACE_ROOT}/app/build/outputs/apk/release"
    "${WORKSPACE_ROOT}/app/build/outputs/apk"
    "${WORKSPACE_ROOT}/build/outputs/apk"
)

FOUND_COUNT=0

for SRC_DIR in "${CANDIDATE_PATHS[@]}"; do
    if [ -d "${SRC_DIR}" ]; then
        # Find all .apk files in the directory
        while IFS= read -r -d '' apk_file; do
            if [ -f "${apk_file}" ]; then
                apk_name="$(basename "${apk_file}")"
                echo "[AeroCode] Found APK: ${apk_file}"
                
                # Copy to workspace output directory
                cp -f "${apk_file}" "${TARGET_DIR_WORKSPACE}/${apk_name}"
                echo "  -> Copied to: ${TARGET_DIR_WORKSPACE}/${apk_name}"
                
                # Copy to /output if accessible
                if [ -d "${TARGET_DIR_ROOT}" ] && [ -w "${TARGET_DIR_ROOT}" ]; then
                    cp -f "${apk_file}" "${TARGET_DIR_ROOT}/${apk_name}"
                    echo "  -> Copied to: ${TARGET_DIR_ROOT}/${apk_name}"
                fi
                
                FOUND_COUNT=$((FOUND_COUNT + 1))
            fi
        done < <(find "${SRC_DIR}" -maxdepth 2 -type f -name "*.apk" -print0 2>/dev/null)
    fi
done

if [ "${FOUND_COUNT}" -eq 0 ]; then
    echo "[AeroCode] Warning: No APK found yet in candidate build folders."
    echo "[AeroCode] Run 'gradle assembleDebug' to build the APK first."
else
    echo "[AeroCode] Successfully exported ${FOUND_COUNT} APK artifact(s) to:"
    echo "  - ${TARGET_DIR_WORKSPACE}"
    if [ -d "${TARGET_DIR_ROOT}" ] && [ -w "${TARGET_DIR_ROOT}" ]; then
        echo "  - ${TARGET_DIR_ROOT}"
    fi
    echo "[AeroCode] Ready for download!"
fi
