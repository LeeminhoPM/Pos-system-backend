#!/usr/bin/env bash
# ========================================================
# SkyPOS Production Database Restoration Script
# Usage: ./restore-db.sh <path_to_backup.sql.gz>
# ========================================================

set -euo pipefail

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <path_to_backup_file.sql.gz>"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Error: Backup file not found: ${BACKUP_FILE}"
    exit 1
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-pos_system}"
DB_USER="${DB_USERNAME:-root}"
DB_PASS="${DB_PASSWORD:-rootpassword}"

echo "========================================================"
echo "Restoring Database: ${DB_NAME} from ${BACKUP_FILE}"
echo "========================================================"

# Verify checksum if present
if [ -f "${BACKUP_FILE}.sha256" ]; then
    echo "Verifying SHA256 Checksum..."
    sha256sum -c "${BACKUP_FILE}.sha256"
fi

# Decompress and import
gunzip -c "${BACKUP_FILE}" | mysql \
  --host="${DB_HOST}" \
  --port="${DB_PORT}" \
  --user="${DB_USER}" \
  --password="${DB_PASS}" \
  --default-character-set=utf8mb4 \
  "${DB_NAME}"

echo "Database restoration completed successfully at $(date)!"
