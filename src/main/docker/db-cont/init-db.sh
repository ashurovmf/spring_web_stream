#!/bin/bash
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	SELECT 1;
EOSQL
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -f /tmp/mktstore.sql