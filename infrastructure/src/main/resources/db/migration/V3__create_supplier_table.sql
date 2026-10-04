CREATE TABLE supplier (
    id     UUID         PRIMARY KEY,
    name   VARCHAR(100) COLLATE "es-x-icu" NOT NULL,
    tax_id VARCHAR(20),
    phone  VARCHAR(30),
    email  VARCHAR(100)
);
