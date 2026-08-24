# MOA database migration

`V20260824__core_query_indexes.sql` adds idempotent composite indexes for the
notification, account-verification, party, payment, deposit, community, and
subscription query paths.

## Production procedure

1. Connect to the private database through the approved application/bastion
   path and take a consistent backup.

   ```bash
   mysqldump --single-transaction --routines --triggers moa > moa_before_20260824.sql
   ```

2. Record current indexes and free disk space.

   ```sql
   SELECT TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME
   FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
   ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
   ```

3. Apply in a low-traffic maintenance window.

   ```bash
   mysql moa < docs/migrations/V20260824__core_query_indexes.sql
   ```

4. Verify the added indexes, compare `EXPLAIN` plans for the matching MyBatis
   queries, and monitor database CPU, disk, and write latency.

5. If needed, run
   `docs/migrations/rollback/R20260824__drop_core_query_indexes.sql`. It removes
   only indexes from this migration and does not delete application data.

Database schema changes are intentionally separate from the normal application
deployment so a failed index build cannot take down an otherwise healthy API.
