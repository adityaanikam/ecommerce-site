#!/bin/bash

# Load environment variables
source ../.env

# Set backup directory
BACKUP_DIR="/backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="mongodb_backup_${TIMESTAMP}"

# Create backup
mongodump \
  --uri="mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongodb:27017/${MONGO_DB}?authSource=admin" \
  --out="${BACKUP_DIR}/${BACKUP_NAME}"

# Compress backup
cd ${BACKUP_DIR}
tar -czf "${BACKUP_NAME}.tar.gz" "${BACKUP_NAME}"
rm -rf "${BACKUP_NAME}"

# Upload to S3
aws s3 cp "${BACKUP_NAME}.tar.gz" "s3://${BACKUP_S3_BUCKET}/mongodb/${BACKUP_NAME}.tar.gz"

# Clean up old backups (local)
find ${BACKUP_DIR} -name "mongodb_backup_*.tar.gz" -type f -mtime +${BACKUP_RETENTION_DAYS} -delete

# Clean up old backups (S3)
aws s3 ls "s3://${BACKUP_S3_BUCKET}/mongodb/" | \
  awk '{print $4}' | \
  grep "mongodb_backup_.*\.tar\.gz" | \
  while read -r backup; do
    backup_date=$(echo $backup | grep -o "[0-9]\{8\}")
    if [ $(( ($(date +%s) - $(date -d "${backup_date}" +%s)) / 86400 )) -gt ${BACKUP_RETENTION_DAYS} ]; then
      aws s3 rm "s3://${BACKUP_S3_BUCKET}/mongodb/${backup}"
    fi
  done
