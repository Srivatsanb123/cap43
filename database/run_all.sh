#!/usr/bin/env bash
# Rebuilds the LOCAL dev database from scratch. DESTROYS all cap43 data.
set -e
cd "$(dirname "$0")"
: "${DB_PASSWORD:?Set DB_PASSWORD first}"
export MYSQL_PWD="$DB_PASSWORD"
for f in 00_reset_dev.sql 01_schema.sql 02_seed_reference.sql 03_seed_demo.sql 04_sync_user.sql; do
  echo "Running $f"
  mysql -u srivatsan < "$f"
done
echo "Database rebuilt."
