# ========================================================
# SkyPOS Windows PowerShell Database Backup Script
# ========================================================
param (
    [string]$DbHost = "localhost",
    [int]$DbPort = 3307,
    [string]$DbName = "pos_system",
    [string]$DbUser = "root",
    [string]$DbPass = "123456",
    [string]$BackupDir = ".\backups\mysql",
    [int]$RetentionDays = 14
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
}

$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = Join-Path $BackupDir "$($DbName)_backup_$($Timestamp).sql"

Write-Host "Starting backup for database: $DbName on $DbHost:$DbPort..." -ForegroundColor Cyan

# Execute mysqldump
& mysqldump --host=$DbHost --port=$DbPort --user=$DbUser --password=$DbPass `
    --single-transaction --quick --routines --triggers --default-character-set=utf8mb4 `
    $DbName > $BackupFile

Write-Host "Backup completed: $BackupFile" -ForegroundColor Green

# Retention policy: Prune older than RetentionDays
$CutoffDate = (Get-Date).AddDays(-$RetentionDays)
Get-ChildItem -Path $BackupDir -Filter "$($DbName)_backup_*.sql" | Where-Object { $_.LastWriteTime -lt $CutoffDate } | Remove-Item -Force
Write-Host "Pruned backups older than $RetentionDays days." -ForegroundColor Yellow
