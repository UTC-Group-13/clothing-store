CREATE DATABASE ecommerce;
USE ecommerce;
DROP TABLE IF EXISTS user_review;
DROP TABLE IF EXISTS order_line;
DROP TABLE IF EXISTS shop_order;
DROP TABLE IF EXISTS order_status;
DROP TABLE IF EXISTS shipping_method;
DROP TABLE IF EXISTS shopping_cart_item;
DROP TABLE IF EXISTS shopping_cart;
DROP TABLE IF EXISTS user_payment_method;
DROP TABLE IF EXISTS payment_type;
DROP TABLE IF EXISTS product_configuration;
DROP TABLE IF EXISTS variation_option;
DROP TABLE IF EXISTS variation;
DROP TABLE IF EXISTS product_item;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS promotion_category;
DROP TABLE IF EXISTS promotion;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS site_user;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS country;
CREATE TABLE country
(
    id           INT AUTO_INCREMENT,
    country_name VARCHAR(500),
    CONSTRAINT pk_country PRIMARY KEY (id)
);
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
    CONSTRAINT fk_add_country FOREIGN KEY (country_id) REFERENCES
        country (id)
);
CREATE TABLE site_user
(
    id            INT AUTO_INCREMENT,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email_address VARCHAR(350) UNIQUE,
    phone_number  VARCHAR(20),
    password      VARCHAR(500) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    CONSTRAINT pk_user PRIMARY KEY (id)
);
CREATE TABLE user_address
(
    user_id    INT,
    address_id INT,
    is_default INT,
    CONSTRAINT fk_useradd_user FOREIGN KEY (user_id) REFERENCES
        site_user (id),
    CONSTRAINT fk_useradd_address FOREIGN KEY (address_id) REFERENCES
        address (id)
);
CREATE TABLE product_category
(
    id                 INT AUTO_INCREMENT,
    parent_category_id INT,
    category_name      VARCHAR(200),
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id)
        REFERENCES product_category (id)
);
CREATE TABLE promotion
(
    id            INT AUTO_INCREMENT,
    name          VARCHAR(200),
    description   VARCHAR(2000),
    discount_rate INT,
    start_date    DATETIME,
    end_date      DATETIME,
    CONSTRAINT pk_promo PRIMARY KEY (id)
);
CREATE TABLE promotion_category
(
    category_id  INT,
    promotion_id INT,
    CONSTRAINT fk_promocat_category FOREIGN KEY (category_id) REFERENCES
        product_category (id),
    CONSTRAINT fk_promocat_promo FOREIGN KEY (promotion_id) REFERENCES
        promotion (id)
);
CREATE TABLE product
(
    id            INT AUTO_INCREMENT,
    category_id   INT,
    name          VARCHAR(500),
    description   VARCHAR(4000),
    product_image VARCHAR(1000),
    CONSTRAINT pk_product PRIMARY KEY (id)
);
CREATE TABLE product_item
(
    id            INT AUTO_INCREMENT,
    product_id    INT,
    sku           VARCHAR(20),
    qty_in_stock  INT,
    product_image VARCHAR(1000),
    price         INT,
    CONSTRAINT pk_productitem PRIMARY KEY (id),
    CONSTRAINT fk_proditem_product FOREIGN KEY (product_id) REFERENCES
        product (id)
);
CREATE TABLE variation
(
    id          INT AUTO_INCREMENT,
    category_id INT,
    name        VARCHAR(500),
    CONSTRAINT pk_variation PRIMARY KEY (id),
    CONSTRAINT fk_variation_category FOREIGN KEY (category_id)
        REFERENCES product_category (id)
);
CREATE TABLE variation_option
(
    id           INT AUTO_INCREMENT,
    variation_id INT,
    value        VARCHAR(200),
    CONSTRAINT pk_varoption PRIMARY KEY (id),
    CONSTRAINT fk_varoption_variation FOREIGN KEY (variation_id)
        REFERENCES variation (id)
);
CREATE TABLE product_configuration
(
    product_item_id     INT,
    variation_option_id INT,
    CONSTRAINT fk_prodconf_proditem FOREIGN KEY (product_item_id)
        REFERENCES product_item (id),
    CONSTRAINT fk_prodconf_varoption FOREIGN KEY (variation_option_id)
        REFERENCES variation_option (id)
);
CREATE TABLE payment_type
(
    id    INT AUTO_INCREMENT,
    value VARCHAR(100),
    CONSTRAINT pk_paymenttype PRIMARY KEY (id)
);
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
    CONSTRAINT fk_userpm_user FOREIGN KEY (user_id) REFERENCES site_user
        (id),
    CONSTRAINT fk_userpm_paytype FOREIGN KEY (payment_type_id)
        REFERENCES payment_type (id)
);
CREATE TABLE shopping_cart
(
    id      INT AUTO_INCREMENT,
    user_id INT,
    CONSTRAINT pk_shopcart PRIMARY KEY (id),
    CONSTRAINT fk_shopcart_user FOREIGN KEY (user_id) REFERENCES
        site_user (id)
);
CREATE TABLE shopping_cart_item
(
    id              INT AUTO_INCREMENT,
    cart_id         INT,
    product_item_id INT,
    qty             INT,
    CONSTRAINT pk_shopcartitem PRIMARY KEY (id),
    CONSTRAINT fk_shopcartitem_shopcart FOREIGN KEY (cart_id) REFERENCES
        shopping_cart (id),
    CONSTRAINT fk_shopcartitem_proditem FOREIGN KEY (product_item_id)
        REFERENCES product_item (id)
);
CREATE TABLE shipping_method
(
    id    INT AUTO_INCREMENT,
    name  VARCHAR(100),
    price INT,
    CONSTRAINT pk_shipmethod PRIMARY KEY (id)
);
CREATE TABLE order_status
(
    id     INT AUTO_INCREMENT,
    status VARCHAR(100),
    CONSTRAINT pk_orderstatus PRIMARY KEY (id)
);
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
    CONSTRAINT fk_shoporder_user FOREIGN KEY (user_id) REFERENCES
        site_user (id),
    CONSTRAINT fk_shoporder_paymethod FOREIGN KEY (payment_method_id)
        REFERENCES user_payment_method (id),
    CONSTRAINT fk_shoporder_shipaddress FOREIGN KEY (shipping_address)
        REFERENCES address (id),
    CONSTRAINT fk_shoporder_shipmethod FOREIGN KEY (shipping_method)
        REFERENCES shipping_method (id),
    CONSTRAINT fk_shoporder_status FOREIGN KEY (order_status) REFERENCES
        order_status (id)
);
CREATE TABLE order_line
(
    id              INT AUTO_INCREMENT,
    product_item_id INT,
    order_id        INT,
    qty             INT,
    price           INT,
    CONSTRAINT pk_orderline PRIMARY KEY (id),
    CONSTRAINT fk_orderline_proditem FOREIGN KEY (product_item_id)
        REFERENCES product_item (id),
    CONSTRAINT fk_orderline_order FOREIGN KEY (order_id) REFERENCES
        shop_order (id)
);
CREATE TABLE user_review
(
    id                 INT AUTO_INCREMENT,
    user_id            INT,
    ordered_product_id INT,
    rating_value       INT,
    comment            VARCHAR(2000),
    CONSTRAINT pk_review PRIMARY KEY (id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES site_user
        (id),
    CONSTRAINT fk_review_product FOREIGN KEY (ordered_product_id)
        REFERENCES order_line (id)
);