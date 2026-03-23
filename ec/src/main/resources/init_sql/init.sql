-- ============================================================
--  Database Schema: Hệ thống quần áo (Clothing Store)
--  Created: 2026-03-22
--  Updated: 2026-03-23
-- ============================================================

CREATE DATABASE IF NOT EXISTS clothing_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE clothing_db;

-- ============================================================
--  DROP TABLES (reverse dependency order)
-- ============================================================
DROP TABLE IF EXISTS user_review;
DROP TABLE IF EXISTS order_line;
DROP TABLE IF EXISTS shop_order;
DROP TABLE IF EXISTS order_status;
DROP TABLE IF EXISTS shipping_method;
DROP TABLE IF EXISTS shopping_cart_item;
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS user_payment_method;
DROP TABLE IF EXISTS payment_type;
DROP TABLE IF EXISTS variant_stocks;
DROP TABLE IF EXISTS product_variants;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS sizes;
DROP TABLE IF EXISTS colors;
DROP TABLE IF EXISTS promotion_category;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS site_user;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS country;

-- ============================================================
--  AUTHENTICATION & USER TABLES
-- ============================================================

CREATE TABLE country
(
    id           INT AUTO_INCREMENT,
    country_name VARCHAR(500),
    CONSTRAINT pk_country PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE address
(
    id            INT AUTO_INCREMENT,
    unit_number   VARCHAR(20),
    street_number VARCHAR(20),
    address_line1 VARCHAR(500),
    address_line2 VARCHAR(500),
    city          VARCHAR(200),
    region        VARCHAR(200),
    postal_code   VARCHAR(20),
    country_id    INT,
    CONSTRAINT pk_address PRIMARY KEY (id),
    CONSTRAINT fk_add_country FOREIGN KEY (country_id) REFERENCES country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE site_user
(
    id            INT AUTO_INCREMENT,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email_address VARCHAR(350) UNIQUE,
    phone_number  VARCHAR(20),
    password      VARCHAR(500) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    CONSTRAINT pk_user PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_address
(
    user_id    INT,
    address_id INT,
    is_default INT,
    CONSTRAINT fk_useradd_user FOREIGN KEY (user_id) REFERENCES site_user (id),
    CONSTRAINT fk_useradd_address FOREIGN KEY (address_id) REFERENCES address (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  PRODUCT CATALOG TABLES (NEW DESIGN FOR CLOTHING)
-- ============================================================

-- 1. Danh mục sản phẩm (hỗ trợ danh mục cha/con)
CREATE TABLE categories (
  id          INT           NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100)  NOT NULL,
  slug        VARCHAR(100)  NOT NULL,
  parent_id   INT           DEFAULT NULL,
  description TEXT          DEFAULT NULL,
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE  KEY uq_categories_slug (slug),
  CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id)
    REFERENCES categories (id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Màu sắc
CREATE TABLE colors (
  id        INT          NOT NULL AUTO_INCREMENT,
  name      VARCHAR(50)  NOT NULL,
  hex_code  CHAR(7)      NOT NULL COMMENT 'Vi du: #FF5733',
  slug      VARCHAR(50)  NOT NULL,

  PRIMARY KEY (id),
  UNIQUE KEY uq_colors_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Size (hỗ trợ nhiều loại: clothing, numeric, shoes)
CREATE TABLE sizes (
  id         INT          NOT NULL AUTO_INCREMENT,
  label      VARCHAR(20)  NOT NULL COMMENT 'S, M, L, XL, 28, 30, 36...',
  type       VARCHAR(20)  NOT NULL COMMENT 'clothing | numeric | shoes',
  sort_order INT          NOT NULL DEFAULT 0,

  PRIMARY KEY (id),
  UNIQUE KEY uq_sizes_label_type (label, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Sản phẩm
CREATE TABLE products (
  id          INT             NOT NULL AUTO_INCREMENT,
  name        VARCHAR(200)    NOT NULL,
  slug        VARCHAR(200)    NOT NULL,
  description TEXT            DEFAULT NULL,
  category_id INT             NOT NULL,
  base_price  DECIMAL(12,2)   NOT NULL,
  brand       VARCHAR(100)    DEFAULT NULL,
  material    VARCHAR(100)    DEFAULT NULL,
  is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE  KEY uq_products_slug (slug),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id)
    REFERENCES categories (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  INDEX idx_products_category  (category_id),
  INDEX idx_products_price     (base_price),
  INDEX idx_products_brand     (brand),
  INDEX idx_products_active    (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Biến thể sản phẩm theo màu
CREATE TABLE product_variants (
  id              INT           NOT NULL AUTO_INCREMENT,
  product_id      INT           NOT NULL,
  color_id        INT           NOT NULL,
  color_image_url VARCHAR(500)  DEFAULT NULL COMMENT 'Anh thumbnail dai dien cho mau nay',
  images          JSON          DEFAULT NULL COMMENT 'Mang URL anh cua mau nay',
  is_default      BOOLEAN       NOT NULL DEFAULT FALSE COMMENT 'Mau hien thi mac dinh',

  PRIMARY KEY (id),
  UNIQUE  KEY uq_variant_product_color (product_id, color_id),
  CONSTRAINT fk_variants_product FOREIGN KEY (product_id)
    REFERENCES products (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_variants_color FOREIGN KEY (color_id)
    REFERENCES colors (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  INDEX idx_variants_product (product_id),
  INDEX idx_variants_color   (color_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Tồn kho theo biến thể + size
CREATE TABLE variant_stocks (
  id             INT            NOT NULL AUTO_INCREMENT,
  variant_id     INT            NOT NULL,
  size_id        INT            NOT NULL,
  stock_qty      INT            NOT NULL DEFAULT 0,
  price_override DECIMAL(12,2)  DEFAULT NULL COMMENT 'Gia rieng, NULL = dung base_price',
  sku            VARCHAR(100)   NOT NULL COMMENT 'Ma hang duy nhat',

  PRIMARY KEY (id),
  UNIQUE  KEY uq_stock_variant_size (variant_id, size_id),
  UNIQUE  KEY uq_stock_sku          (sku),
  CONSTRAINT fk_stocks_variant FOREIGN KEY (variant_id)
    REFERENCES product_variants (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_stocks_size FOREIGN KEY (size_id)
    REFERENCES sizes (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

  INDEX idx_stocks_variant   (variant_id),
  INDEX idx_stocks_size      (size_id),
  INDEX idx_stocks_qty       (stock_qty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  PROMOTION TABLES
-- ============================================================

CREATE TABLE promotion
(
    id            INT AUTO_INCREMENT,
    name          VARCHAR(200),
    description   VARCHAR(2000),
    discount_rate INT,
    start_date    DATETIME,
    end_date      DATETIME,
    CONSTRAINT pk_promo PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE promotion_category
(
    category_id  INT,
    promotion_id INT,
    CONSTRAINT fk_promocat_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_promocat_promo FOREIGN KEY (promotion_id) REFERENCES promotion (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  PAYMENT & SHOPPING TABLES
-- ============================================================

CREATE TABLE payment_type
(
    id    INT AUTO_INCREMENT,
    value VARCHAR(100),
    CONSTRAINT pk_paymenttype PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_payment_method
(
    id              INT AUTO_INCREMENT,
    user_id         INT,
    payment_type_id INT,
    provider        VARCHAR(100),
    account_number  VARCHAR(50),
    expiry_date     DATE,
    is_default      INT,
    CONSTRAINT pk_userpm PRIMARY KEY (id),
    CONSTRAINT fk_userpm_user FOREIGN KEY (user_id) REFERENCES site_user (id),
    CONSTRAINT fk_userpm_paytype FOREIGN KEY (payment_type_id) REFERENCES payment_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shopping_cart
(
    id      INT AUTO_INCREMENT,
    user_id INT,
    CONSTRAINT pk_shopcart PRIMARY KEY (id),
    CONSTRAINT fk_shopcart_user FOREIGN KEY (user_id) REFERENCES site_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shopping_cart_item
(
    id               INT AUTO_INCREMENT,
    cart_id          INT,
    variant_stock_id INT,
    qty              INT,
    CONSTRAINT pk_shopcartitem PRIMARY KEY (id),
    CONSTRAINT fk_shopcartitem_shopcart FOREIGN KEY (cart_id) REFERENCES shopping_cart (id),
    CONSTRAINT fk_shopcartitem_varstock FOREIGN KEY (variant_stock_id) REFERENCES variant_stocks (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  ORDER TABLES
-- ============================================================

CREATE TABLE shipping_method
(
    id    INT AUTO_INCREMENT,
    name  VARCHAR(100),
    price INT,
    CONSTRAINT pk_shipmethod PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_status
(
    id     INT AUTO_INCREMENT,
    status VARCHAR(100),
    CONSTRAINT pk_orderstatus PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shop_order
(
    id                INT AUTO_INCREMENT,
    user_id           INT,
    order_date        DATETIME,
    payment_method_id INT,
    shipping_address  INT,
    shipping_method   INT,
    order_total       INT,
    order_status      INT,
    CONSTRAINT pk_shoporder PRIMARY KEY (id),
    CONSTRAINT fk_shoporder_user FOREIGN KEY (user_id) REFERENCES site_user (id),
    CONSTRAINT fk_shoporder_paymethod FOREIGN KEY (payment_method_id) REFERENCES user_payment_method (id),
    CONSTRAINT fk_shoporder_shipaddress FOREIGN KEY (shipping_address) REFERENCES address (id),
    CONSTRAINT fk_shoporder_shipmethod FOREIGN KEY (shipping_method) REFERENCES shipping_method (id),
    CONSTRAINT fk_shoporder_status FOREIGN KEY (order_status) REFERENCES order_status (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_line
(
    id               INT AUTO_INCREMENT,
    variant_stock_id INT,
    order_id         INT,
    qty              INT,
    price            INT,
    CONSTRAINT pk_orderline PRIMARY KEY (id),
    CONSTRAINT fk_orderline_varstock FOREIGN KEY (variant_stock_id) REFERENCES variant_stocks (id),
    CONSTRAINT fk_orderline_order FOREIGN KEY (order_id) REFERENCES shop_order (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  REVIEW TABLE
-- ============================================================

CREATE TABLE user_review
(
    id                 INT AUTO_INCREMENT,
    user_id            INT,
    ordered_product_id INT,
    rating_value       INT,
    comment            VARCHAR(2000),
    CONSTRAINT pk_review PRIMARY KEY (id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES site_user (id),
    CONSTRAINT fk_review_product FOREIGN KEY (ordered_product_id) REFERENCES order_line (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
