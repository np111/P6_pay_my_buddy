CREATE TABLE `user_balances` (
  `user_id` BIGINT(20) UNSIGNED NOT NULL,
  `currency` CHAR(3) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `amount` DECIMAL(40, 20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`, `currency`),
  INDEX `user_id` (`user_id`),
  CONSTRAINT `fk__user_balances__users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = COMPACT;
