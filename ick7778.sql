/*
 Navicat Premium Data Transfer

 Source Server         : 777
 Source Server Type    : MySQL
 Source Server Version : 50738 (5.7.38-log)
 Source Host           : localhost:3306
 Source Schema         : ick7778

 Target Server Type    : MySQL
 Target Server Version : 50738 (5.7.38-log)
 File Encoding         : 65001

 Date: 11/03/2026 20:38:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for item_images
-- ----------------------------
DROP TABLE IF EXISTS `item_images`;
CREATE TABLE `item_images`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(20) NOT NULL,
  `image_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sort_order` int(11) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_item_images_item`(`item_id`) USING BTREE,
  CONSTRAINT `fk_item_images_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_images
-- ----------------------------

-- ----------------------------
-- Table structure for items
-- ----------------------------
DROP TABLE IF EXISTS `items`;
CREATE TABLE `items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `price` decimal(12, 2) NOT NULL,
  `status` enum('AVAILABLE','OFF_SHELF','RESERVED','SOLD') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `title` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `seller_id` bigint(20) NOT NULL,
  `version` bigint(20) NULL DEFAULT NULL,
  `cover_image_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `board` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'daily',
  `delete_category` enum('NONE','NO_ORDER','ORDER_FINISHED','ORDER_UNFINISHED') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKsm9ro5ntn6yaav2m7ydato0fc`(`seller_id`) USING BTREE,
  CONSTRAINT `FKsm9ro5ntn6yaav2m7ydato0fc` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of items
-- ----------------------------
INSERT INTO `items` VALUES (1, '2026-02-27 06:54:01.856195', '???', 99.00, 'OFF_SHELF', '????', 1, NULL, NULL, 'daily', 'NONE', 0);
INSERT INTO `items` VALUES (2, '2026-02-27 08:18:01.468209', '333', 222.00, 'OFF_SHELF', '111', 2, NULL, NULL, 'daily', 'NONE', 0);
INSERT INTO `items` VALUES (3, '2026-02-27 08:26:50.725821', '111', 111.00, 'OFF_SHELF', '1111', 2, NULL, NULL, 'daily', 'NONE', 0);
INSERT INTO `items` VALUES (4, '2026-02-27 08:38:48.698259', '11', 11.00, 'OFF_SHELF', '11', 2, NULL, NULL, 'daily', 'NONE', 0);
INSERT INTO `items` VALUES (5, '2026-02-28 13:39:47.007833', '2123', 12.00, 'OFF_SHELF', '777', 2, 4, NULL, 'daily', 'NONE', 1);
INSERT INTO `items` VALUES (6, '2026-03-10 13:02:08.200023', '二手', 123.00, 'OFF_SHELF', '相机', 3, 7, '/uploads/items/4ea4da67-689f-471e-a73e-32a406b9b153.jpg', 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (7, '2026-03-10 14:23:54.115173', '', 132.00, 'OFF_SHELF', '77', 3, 8, '/uploads/items/f49d1f99-709f-49ae-8fbc-31afd765933d.jpg', 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (8, '2026-03-10 15:19:37.954801', '111', 134.00, 'OFF_SHELF', '二手相机', 2, 2, '/uploads/items/082d4845-7912-40c8-8878-0c04d6f850fa.jpg', 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (9, '2026-03-10 16:05:10.104316', '111', 111.00, 'OFF_SHELF', '111', 2, 2, NULL, 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (10, '2026-03-11 10:14:51.857375', '13', 123.00, 'OFF_SHELF', '123', 3, 2, NULL, 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (11, '2026-03-11 11:18:53.616451', '123', 123.00, 'OFF_SHELF', '123', 2, 2, '/uploads/items/0a883f41-89af-4bae-b360-c00a9b0e2995.jpg', 'digital', 'NONE', 1);
INSERT INTO `items` VALUES (12, '2026-03-11 11:35:44.634032', '123', 123.00, 'OFF_SHELF', '123', 2, 2, '/uploads/items/212cf21b-9aca-44f9-9f46-75272041ef3e.jpg', 'daily', 'NONE', 1);
INSERT INTO `items` VALUES (13, '2026-03-11 11:44:34.302983', '123', 123.00, 'OFF_SHELF', '123', 3, 2, '/uploads/items/d374c6cd-5b83-4864-936f-bd8ce0b13051.jpg', 'digital', 'NONE', 1);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` enum('CANCELED','CREATED','PAID') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `buyer_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKrg95ddumc8608v80pqy2m43h0`(`item_id`) USING BTREE,
  INDEX `FKhtx3insd5ge6w486omk4fnk54`(`buyer_id`) USING BTREE,
  CONSTRAINT `FK247nnxschdfm8lre0ssvy3k1r` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKhtx3insd5ge6w486omk4fnk54` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'CANCELED', 3, 5, '2026-03-10 19:50:12.000000');

-- ----------------------------
-- Table structure for user_behavior
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `item_id` bigint(20) NOT NULL,
  `type` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKibcr6q081mffjflsr9cechor8`(`item_id`) USING BTREE,
  INDEX `FKaycmqn76momq89eohbikdj2a0`(`user_id`) USING BTREE,
  CONSTRAINT `FKaycmqn76momq89eohbikdj2a0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKibcr6q081mffjflsr9cechor8` FOREIGN KEY (`item_id`) REFERENCES `items` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_behavior
-- ----------------------------
INSERT INTO `user_behavior` VALUES (1, 2, 4, 'VIEW', '2026-02-28 14:39:16');
INSERT INTO `user_behavior` VALUES (2, 2, 3, 'VIEW', '2026-02-28 21:08:48');
INSERT INTO `user_behavior` VALUES (3, 2, 2, 'VIEW', '2026-02-28 21:38:22');
INSERT INTO `user_behavior` VALUES (4, 2, 1, 'VIEW', '2026-02-28 21:38:46');
INSERT INTO `user_behavior` VALUES (5, 2, 5, 'VIEW', '2026-02-28 21:39:48');
INSERT INTO `user_behavior` VALUES (6, 3, 5, 'VIEW', '2026-03-08 18:04:36');
INSERT INTO `user_behavior` VALUES (7, 2, 5, 'VIEW', '2026-03-10 19:31:30');
INSERT INTO `user_behavior` VALUES (8, 3, 5, 'VIEW', '2026-03-10 20:11:45');
INSERT INTO `user_behavior` VALUES (9, 3, 6, 'VIEW', '2026-03-10 21:02:11');
INSERT INTO `user_behavior` VALUES (10, 2, 6, 'VIEW', '2026-03-10 21:21:39');
INSERT INTO `user_behavior` VALUES (11, 3, 7, 'VIEW', '2026-03-10 22:23:58');
INSERT INTO `user_behavior` VALUES (12, 2, 8, 'VIEW', '2026-03-11 00:36:29');
INSERT INTO `user_behavior` VALUES (13, 3, 4, 'VIEW', '2026-03-11 02:10:52');
INSERT INTO `user_behavior` VALUES (14, 3, 5, 'VIEW', '2026-03-11 17:33:23');
INSERT INTO `user_behavior` VALUES (15, 3, 7, 'VIEW', '2026-03-11 18:03:34');
INSERT INTO `user_behavior` VALUES (16, 3, 6, 'VIEW', '2026-03-11 18:25:48');
INSERT INTO `user_behavior` VALUES (17, 2, 4, 'VIEW', '2026-03-11 19:08:42');
INSERT INTO `user_behavior` VALUES (18, 2, 6, 'VIEW', '2026-03-11 19:18:58');
INSERT INTO `user_behavior` VALUES (19, 2, 11, 'VIEW', '2026-03-11 19:19:04');
INSERT INTO `user_behavior` VALUES (20, 2, 12, 'VIEW', '2026-03-11 19:35:55');
INSERT INTO `user_behavior` VALUES (21, 3, 13, 'VIEW', '2026-03-11 20:30:22');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password_hash` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `username` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `avatar_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `bio` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `wechat_qr_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '$2a$10$NBK.ZiUDxVtOCnjkyk9/q.XqTpZgvuYM8Ou0h2XtWADFLDxGtx/Py', 'USER', 'u1', 'u1', NULL, NULL, NULL);
INSERT INTO `users` VALUES (2, '$2a$10$wBwTbPIQ24LN0pDt4nnCu.gmOGI7lowvMsOZsg6iiiTsgsEuEbr8e', 'USER', '777ick', '777ick', NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, '$2a$10$A3gMcNu7EpxfE75CXOWK6ezVMaS77/LA9LnR4dN9Q.vzpQ6SWhTIi', 'ADMIN', 'admin', 'admin', NULL, NULL, NULL);
INSERT INTO `users` VALUES (4, '$2a$10$ixHGFC7pH4rauR7.B.XdMeMDEcODpu1701LqQDVCkDCrU3x8DU39O', 'USER', 'ick777', 'ick777', NULL, NULL, NULL);
INSERT INTO `users` VALUES (5, '$2a$10$Oa7L8h4UNX40M6ZYqzlasOKBRxmrK7kbWDEqTIDvdoc12q8iHPtWe', 'USER', '7777ick', '7777ick', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
