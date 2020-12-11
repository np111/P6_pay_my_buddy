CREATE TABLE `user_contacts` (
  `user_id` BIGINT(20) UNSIGNED NOT NULL,
  `contact_id` BIGINT(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`user_id`, `contact_id`),
  INDEX `user_id` (`user_id`),
  INDEX `contact_id` (`contact_id`),
  CONSTRAINT `fk__user_contacts__users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk__user_contacts__users__contact` FOREIGN KEY (`contact_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 ROW_FORMAT = COMPACT;
