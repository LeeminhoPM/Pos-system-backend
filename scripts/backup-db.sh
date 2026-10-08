#!/usr/bin/env bash
# ========================================================
# SkyPOS Production Database Backup Script
# Usage: ./backup-db.sh [backup_dir]
# ========================================================

set -euo pipefail

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-pos_system}"
DB_USER="${DB_USERNAME:-root}"
DB_PASS="${DB_PASSWORD:-rootpassword}"

BACKUP_DIR="${1:-./backups/mysql}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_backup_${TIMESTAMP}.sql.gz"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"

mkdir -p "${BACKUP_DIR}"

echo "========================================================"
echo "Starting SkyPOS Database Backup at $(date)"
echo "Target Host: ${DB_HOST}:${DB_PORT}, Database: ${DB_NAME}"
echo "========================================================"

# Execute single-transaction consistent mysqldump piped through gzip
mysqldump \
  --host="${DB_HOST}" \
  --port="${DB_PORT}" \
  --user="${DB_USER}" \
  --password="${DB_PASS}" \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --default-character-set=utf8mb4 \
  "${DB_NAME}" | gzip -9 > "${BACKUP_FILE}"

# Generate SHA256 checksum for verification
sha256sum "${BACKUP_FILE}" > "${BACKUP_FILE}.sha256"

BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "Backup successfully created: ${BACKUP_FILE} (Size: ${BACKUP_SIZE})"

# Retention Policy: Prune backups older than RETENTION_DAYS
echo "Pruning backups older than ${RETENTION_DAYS} days in ${BACKUP_DIR}..."
find "${BACKUP_DIR}" -name "${DB_NAME}_backup_*.sql.gz*" -mtime +"${RETENTION_DAYS}" -exec rm -f {} +

echo "Backup and rotation completed successfully!"
