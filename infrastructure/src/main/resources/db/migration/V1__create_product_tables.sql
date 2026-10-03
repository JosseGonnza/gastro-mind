CREATE TABLE product (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(20)  NOT NULL,
    unit        VARCHAR(20)  NOT NULL
);

CREATE TABLE product_allergen (
    product_id UUID        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    allergen   VARCHAR(20) NOT NULL,
    PRIMARY KEY (product_id, allergen)
);
