-- Rollback for V20260824__core_query_indexes.sql (MySQL 8.x)

DROP PROCEDURE IF EXISTS drop_index_if_present;

DELIMITER $$
CREATE PROCEDURE drop_index_if_present(
    IN requested_table VARCHAR(64),
    IN requested_index VARCHAR(64)
)
BEGIN
    DECLARE actual_table VARCHAR(64) DEFAULT NULL;

    SELECT MAX(TABLE_NAME)
      INTO actual_table
      FROM INFORMATION_SCHEMA.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND LOWER(TABLE_NAME) = LOWER(requested_table);

    IF actual_table IS NOT NULL
       AND EXISTS (
           SELECT 1
             FROM INFORMATION_SCHEMA.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE()
              AND LOWER(TABLE_NAME) = LOWER(actual_table)
              AND INDEX_NAME = requested_index
       ) THEN
        SET @index_sql = CONCAT(
            'ALTER TABLE `', REPLACE(actual_table, '`', '``'),
            '` DROP INDEX `', REPLACE(requested_index, '`', '``'), '`'
        );
        PREPARE index_statement FROM @index_sql;
        EXECUTE index_statement;
        DEALLOCATE PREPARE index_statement;
    END IF;
END$$
DELIMITER ;

CALL drop_index_if_present('PUSH', 'idx_push_receiver_deleted_sent');
CALL drop_index_if_present('ACCOUNT_VERIFICATION', 'idx_account_verification_user_created');
CALL drop_index_if_present('PARTY', 'idx_party_status_registered');
CALL drop_index_if_present('PARTY', 'idx_party_leader_registered');
CALL drop_index_if_present('PAYMENT', 'idx_payment_user_date');
CALL drop_index_if_present('PAYMENT', 'idx_payment_party_date');
CALL drop_index_if_present('DEPOSIT', 'idx_deposit_user_date');
CALL drop_index_if_present('COMMUNITY', 'idx_community_code_created');
CALL drop_index_if_present('SUBSCRIPTION', 'idx_subscription_user_id');

DROP PROCEDURE IF EXISTS drop_index_if_present;
