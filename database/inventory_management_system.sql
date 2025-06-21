
-- =======================
-- Tabela billing
-- =======================
CREATE TABLE billing (
    id SERIAL PRIMARY KEY,
    item_number VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(50, 2) NOT NULL,
    total_amount NUMERIC(50, 2) NOT NULL
);

-- =======================
-- Tabela customers
-- =======================
CREATE TABLE customers (
    id SERIAL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(10) PRIMARY KEY
);

INSERT INTO customers (id, name, phone_number) VALUES
(24, 'Shahzaib', '7070564601'),
(23, 'Rashid Makki', '7070564603'),
(25, 'Shadab', '9818977363');

-- =======================
-- Tabela inv_seq
-- =======================
CREATE TABLE inv_seq (
    next_not_cached_value BIGINT NOT NULL,
    minimum_value BIGINT NOT NULL,
    maximum_value BIGINT NOT NULL,
    start_value BIGINT NOT NULL,
    increment BIGINT NOT NULL,
    cache_size BIGINT NOT NULL,
    cycle_option BOOLEAN NOT NULL,
    cycle_count BIGINT NOT NULL
);

INSERT INTO inv_seq VALUES (3001, 1, 99999999999999, 1, 1, 1000, FALSE, 0);

-- =======================
-- Tabela products
-- =======================
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    item_number VARCHAR(255) NOT NULL,
    item_group VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(50, 2) NOT NULL
);

INSERT INTO products (id, item_number, item_group, quantity, price) VALUES
(1, 'AX123456', 'Shirts', 60, 700.00),
(2, 'BX123456', 'Shirts', 100, 300.00),
(3, 'ZX123456', 'Shirts', 40, 800.00);

-- =======================
-- Tabela purchase
-- =======================
CREATE TABLE purchase (
    id SERIAL PRIMARY KEY,
    invoice VARCHAR(255) NOT NULL,
    shop_and_address VARCHAR(1000) NOT NULL,
    total_items INT NOT NULL,
    total_amount NUMERIC(50, 2) NOT NULL,
    date_of_purchase VARCHAR(255) NOT NULL
);

INSERT INTO purchase (id, invoice, shop_and_address, total_items, total_amount, date_of_purchase) VALUES
(1, '123XB123', 'AB, Yamuna Nagar, Delhi', 50, 200000.00, '30/05/2023'),
(2, 'babab', 'abbaban', 100, 100000.00, '29/05/2023');

-- =======================
-- Tabela sales
-- =======================
CREATE TABLE sales (
    id SERIAL PRIMARY KEY,
    inv_num VARCHAR(255) NOT NULL,
    cust_id INT NOT NULL,
    price NUMERIC(50, 2) NOT NULL,
    quantity INT NOT NULL,
    total_amount NUMERIC(50, 2) NOT NULL,
    date VARCHAR(255) NOT NULL,
    item_number VARCHAR(255) NOT NULL,
    CONSTRAINT fk_sales_customer FOREIGN KEY (cust_id) REFERENCES customers(id)
);

INSERT INTO sales (id, inv_num, cust_id, price, quantity, total_amount, date, item_number) VALUES
(28, 'INV-1', 23, 700.00, 4, 2800.00, '2023-06-03', 'AX123456'),
(29, 'INV-1', 23, 800.00, 2, 1600.00, '2023-06-03', 'ZX123456'),
(30, 'INV-2', 24, 700.00, 4, 2800.00, '2023-06-03', 'AX123456'),
(31, 'INV-3', 25, 900.00, 2, 1800.00, '2023-06-03', 'AX123456'),
(32, 'INV-3', 25, 800.00, 1, 800.00, '2023-06-03', 'ZX123456'),
(33, 'INV-4', 23, 700.00, 2, 1400.00, '2023-06-04', 'AX123456'),
(34, 'INV-5', 23, 300.00, 2, 600.00, '2023-06-04', 'BX123456');

-- =======================
-- Tabela users
-- =======================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

INSERT INTO users (id, username, password) VALUES
(1, 'admin', 'admin');
