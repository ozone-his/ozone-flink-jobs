#!/usr/bin/env python3
"""Regenerates the export destination-table DDLs from the analytics schema.

The export queries are `INSERT INTO <table> SELECT t.* FROM <catalog>.analytics.<table>`, so each
export destination table must mirror the analytics table it reads, column for column. Those DDLs
had drifted from the analytics schema (the flattening queries changed shape but the export tables
did not), so they are derived from the live schema instead of hand-maintained.

Usage:  generate-export-tables.py <postgres-container> <analytics-db> <output dir>
Requires the analytics database to be up and migrated.
"""
import subprocess
import sys
from pathlib import Path

# PostgreSQL type -> Flink SQL type. The analytics tables are created by Liquibase; these are the
# types it produces.
TYPES = {
    "bigint": "BIGINT",
    "integer": "INT",
    "smallint": "SMALLINT",
    "text": "STRING",
    "character varying": "STRING",
    "character": "STRING",
    "boolean": "BOOLEAN",
    "timestamp without time zone": "TIMESTAMP",
    "timestamp with time zone": "TIMESTAMP",
    "date": "DATE",
    "numeric": "DECIMAL(18,6)",
    "double precision": "DOUBLE",
    "real": "DOUBLE",
}


def columns(container, db, table):
    out = subprocess.run(
        ["docker", "exec", container, "psql", "-U", "analytics", "-d", db, "-tAF|", "-c",
         "SELECT column_name, data_type FROM information_schema.columns "
         f"WHERE table_name='{table}' ORDER BY ordinal_position"],
        capture_output=True, text=True, check=True).stdout
    cols = []
    for line in out.splitlines():
        if not line.strip():
            continue
        name, pg_type = line.split("|", 1)
        if pg_type not in TYPES:
            raise SystemExit(f"{table}.{name}: unmapped PostgreSQL type {pg_type!r}")
        cols.append((name, TYPES[pg_type]))
    return cols


def main():
    container, db, out_dir = sys.argv[1], sys.argv[2], Path(sys.argv[3])
    # Regenerate exactly the tables that already have an export DDL, so the set stays curated.
    for path in sorted(out_dir.glob("*.sql")):
        table = path.stem
        cols = columns(container, db, table)
        if not cols:
            raise SystemExit(f"{table}: no columns found in analytics schema")
        body = ",\n".join(f"    {n} {t}" for n, t in cols)
        header = (
            f"-- GENERATED from the analytics `{table}` table by development/seed/\n"
            f"-- generate-export-tables.py. The export query does SELECT t.* from it, so the two\n"
            f"-- must match column for column. Do not edit by hand; re-run the generator.\n\n"
        )
        path.write_text(header + f"CREATE TABLE {table} (\n{body}\n)\n")
        print(f"  {table}: {len(cols)} columns")


if __name__ == "__main__":
    main()
