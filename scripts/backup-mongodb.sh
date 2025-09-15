#!/bin/bash

# Load environment variables
source .env

# Configuration
BACKUP_DIR="/backup/mongodb"
BACKUP_NAME="mongodb-backup-$(date +%Y%m%d-%H%M%S)"
RETENTION_DAYS=7

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Perform backup
mongodump \
  --uri="mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongodb:27017/${MONGO_DB}?authSource=admin" \
  --out="${BACKUP_DIR}/${BACKUP_NAME}" \
  --gzip

# Check if backup was successful
if [ $? -eq 0 ]; then
    echo "Backup completed successfully: ${BACKUP_NAME}"
    
    # Create tar archive
    cd $BACKUP_DIR
    tar -czf "${BACKUP_NAME}.tar.gz" $BACKUP_NAME
    rm -rf $BACKUP_NAME
    
    # Upload to cloud storage (uncomment and modify as needed)
    # aws s3 cp "${BACKUP_NAME}.tar.gz" "s3://your-bucket/mongodb-backups/"
    
    # Delete old backups
    find $BACKUP_DIR -name "mongodb-backup-*.tar.gz" -type f -mtime +$RETENTION_DAYS -delete
    
    echo "Backup process completed"
else
    echo "Backup failed"
    exit 1
fi