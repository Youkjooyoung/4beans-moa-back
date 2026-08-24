-- MOA high-frequency query indexes (MySQL 8.x)
--
-- Apply only after a production snapshot. This script is idempotent and resolves
-- actual table-name casing. The matching rollback script removes only these indexes.

DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER $$
CREATE PROCEDURE add_index_if_missing(
    IN requested_table VARCHAR(64),
    IN requested_index VARCHAR(64),
    IN index_columns VARCHAR(512)
)
BEGIN
    DECLARE actual_table VARCHAR(64) DEFAULT NULL;

    SELECT MAX(TABLE_NAME)
      INTO actual_table
      FROM INFORMATION_SCHEMA.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND LOWER(TABLE_NAME) = LOWER(requested_table);

    IF actual_table IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
             FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND LOWER(TABLE_NAME) = LOWER(actual_table)
              AND INDEX_NAME = requested_index
       ) THEN
        SET @index_sql = CONCAT(
            'ALTER TABLE `', REPLACE(actual_table, '`', '``'),
            '` ADD INDEX `', REPLACE(requested_index, '`', '``'),
            '` (', index_columns, ')'
        );
        PREPARE index_statement FROM @index_sql;
        EXECUTE index_statement;
        DEALLOCATE PREPARE index_statement;
    END IF;
END$$
DELIMITER ;

CALL add_index_if_missing('PUSH', 'idx_push_receiver_deleted_sent', '`RECEIVER_ID`, `IS_DELETED`, `SENT_AT`');
CALL add_index_if_missing('ACCOUNT_VERIFICATION', 'idx_account_verification_user_created', '`USER_ID`, `CREATED_AT`');
CALL add_index_if_missing('PARTY', 'idx_party_status_registered', '`PARTY_STATUS`, `REG_DATE`, `PARTY_ID`');
CALL add_index_if_missing('PARTY', 'idx_party_leader_registered', '`PARTY_LEADER_ID`, `REG_DATE`');
CALL add_index_if_missing('PAYMENT', 'idx_payment_user_date', '`USER_ID`, `PAYMENT_DATE`');
CALL add_index_if_missing('PAYMENT', 'idx_payment_party_date', '`PARTY_ID`, `PAYMENT_DATE`');
CALL add_index_if_missing('DEPOSIT', 'idx_deposit_user_date', '`USER_ID`, `PAYMENT_DATE`');
CALL add_index_if_missing('COMMUNITY', 'idx_community_code_created', '`COMMUNITY_CODE_ID`, `CREATED_AT`');
CALL add_index_if_missing('SUBSCRIPTION', 'idx_subscription_user_id', '`USER_ID`, `SUBSCRIPTION_ID`');

DROP PROCEDURE IF EXISTS add_index_if_missing;
