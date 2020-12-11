ALTER TABLE `transactions`
  ADD `fee` DECIMAL(40, 20) NOT NULL DEFAULT 0 AFTER `amount`,
  ADD `description` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL AFTER `fee`;
