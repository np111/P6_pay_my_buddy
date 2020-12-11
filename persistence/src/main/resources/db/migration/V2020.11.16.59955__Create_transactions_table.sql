CREATE TABLE `transactions` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `sender_id` BIGINT(20) UNSIGNED NOT NULL,
  `recipient_id` BIGINT(20) UNSIGNED NOT NULL,
  `currency` CHAR(3) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `amount` DECIMAL(40, 20) NOT NULL DEFAULT 0,
  `date` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `sender_id` (`sender_id`),
  INDEX `recipient_id` (`recipient_id`),
  INDEX `date` (`date`),
  CONSTRAINT `fk__transactions__users__sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk__transactions__users__recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = COMPACT;
