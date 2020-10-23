DROP DATABASE IF EXISTS beerorderservice;
DROP USER IF EXISTS `beer_order_service`@`%`;
CREATE DATABASE IF NOT EXISTS beerorderservice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS `beer_order_service`@`%` IDENTIFIED WITH mysql_native_password BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW,
CREATE ROUTINE, ALTER ROUTINE, EVENT, TRIGGER ON `beerorderservice`.* TO `beer_order_service`@`%`;
FLUSH PRIVILEGES;

USE beerorderservice;

CREATE TABLE `beer_order` (
                                             `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
                                             `created_date` datetime(6) DEFAULT NULL,
                                             `last_modified_date` datetime(6) DEFAULT NULL,
                                             `version` bigint(20) DEFAULT NULL,
                                             `customer_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                             `order_status` int(11) DEFAULT NULL,
                                             `order_status_callback_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                             `customer_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                             PRIMARY KEY (`id`),
                                             KEY `FK5siih2e7vpx70nx4wexpxpji` (`customer_id`),
                                             CONSTRAINT `FK5siih2e7vpx70nx4wexpxpji` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`)
                 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `beer_order_line` (
                                 `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `created_date` datetime(6) DEFAULT NULL,
                                 `last_modified_date` datetime(6) DEFAULT NULL,
                                 `version` bigint(20) DEFAULT NULL,
                                 `beer_id` binary(255) DEFAULT NULL,
                                 `order_quantity` int(11) DEFAULT NULL,
                                 `quantity_allocated` int(11) DEFAULT NULL,
                                 `upc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `beer_order_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FKhkgofxhwx8yw9m3vat8mgtnxs` (`beer_order_id`),
                                 CONSTRAINT `FKhkgofxhwx8yw9m3vat8mgtnxs` FOREIGN KEY (`beer_order_id`) REFERENCES `beer_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `customer` (
                          `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
                          `created_date` datetime(6) DEFAULT NULL,
                          `last_modified_date` datetime(6) DEFAULT NULL,
                          `version` bigint(20) DEFAULT NULL,
                          `api_key` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                          `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;