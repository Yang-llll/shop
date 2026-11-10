USE braised_pork_db;

-- 用户扫码后先进入待确认状态，由管理员确认实际到账。
ALTER TABLE orders
    MODIFY COLUMN status ENUM(
        'PENDING',
        'PAYMENT_REVIEW',
        'PAID',
        'SHIPPED',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL;
