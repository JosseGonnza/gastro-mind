CREATE TABLE goods_receipt (
    id                   UUID        PRIMARY KEY,
    supplier_id          UUID        NOT NULL REFERENCES supplier (id),
    delivery_note_number VARCHAR(50) NOT NULL,
    received_on          DATE        NOT NULL,
    UNIQUE (supplier_id, delivery_note_number)
);

CREATE TABLE batch (
    id               UUID          PRIMARY KEY,
    product_id       UUID          NOT NULL REFERENCES product (id),
    goods_receipt_id UUID          REFERENCES goods_receipt (id),
    receipt_line     SMALLINT,
    lot_code         VARCHAR(50)   NOT NULL,
    entry_date       DATE          NOT NULL,
    expiration_date  DATE          NOT NULL,
    purchase_price   NUMERIC(12,2) NOT NULL CHECK (purchase_price >= 0),
    currency         CHAR(3)       NOT NULL,
    unit             VARCHAR(20)   NOT NULL,
    initial_quantity NUMERIC       NOT NULL CHECK (initial_quantity > 0),
    current_quantity NUMERIC       NOT NULL CHECK (current_quantity >= 0),
    CHECK (current_quantity <= initial_quantity)
);

CREATE INDEX idx_batch_product ON batch (product_id);
CREATE INDEX idx_batch_goods_receipt ON batch (goods_receipt_id);
