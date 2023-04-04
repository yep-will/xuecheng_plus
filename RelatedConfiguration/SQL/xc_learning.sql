/*
 Navicat MySQL Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : xc_learning

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 04/04/2023 09:08:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for xc_choose_course
-- ----------------------------
DROP TABLE IF EXISTS `xc_choose_course`;
CREATE TABLE `xc_choose_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `order_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `course_price` float(10, 2) NOT NULL COMMENT '课程价格',
  `valid_days` int NOT NULL COMMENT '课程有效期(天)',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '选课状态',
  `validtime_start` datetime NOT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '结束服务时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xc_choose_course
-- ----------------------------
INSERT INTO `xc_choose_course` VALUES (10, 2, '测试课程01', '50', 1232141425, '700002', '2022-10-04 14:00:18', 1.00, 555, '701002', '2022-10-04 14:00:18', '2024-04-11 14:00:18', NULL);
INSERT INTO `xc_choose_course` VALUES (11, 117, 'Nacos微服务开发实战', '52', 1232141425, '700002', '2022-10-04 19:25:48', 1.00, 365, '701001', '2022-10-04 19:25:48', '2023-10-04 19:25:48', NULL);
INSERT INTO `xc_choose_course` VALUES (12, 124, '313免费课程测试', '52', 1232141425, '700001', '2023-03-13 21:55:49', 234.00, 365, '701001', '2023-03-13 21:55:49', '2024-03-12 21:55:49', NULL);
INSERT INTO `xc_choose_course` VALUES (13, 123, '313收费课程测试', '52', 1232141425, '700002', '2023-03-13 22:03:53', 131.00, 365, '701002', '2023-03-13 22:03:53', '2024-03-12 22:03:53', NULL);
INSERT INTO `xc_choose_course` VALUES (14, 126, '320收费课程测试', '52', 1232141425, '700002', '2023-03-20 21:38:38', 320.00, 365, '701001', '2023-03-20 21:38:38', '2024-03-19 21:38:38', NULL);

-- ----------------------------
-- Table structure for xc_course_tables
-- ----------------------------
DROP TABLE IF EXISTS `xc_course_tables`;
CREATE TABLE `xc_course_tables`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `choose_course_id` bigint NOT NULL COMMENT '选课订单id',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `course_id` bigint NOT NULL COMMENT '课程id',
  `company_id` bigint NOT NULL COMMENT '机构id',
  `course_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `course_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程类型',
  `create_date` datetime NOT NULL COMMENT '添加时间',
  `validtime_start` datetime NULL DEFAULT NULL COMMENT '开始服务时间',
  `validtime_end` datetime NOT NULL COMMENT '到期时间',
  `update_date` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `course_tables_unique`(`user_id`, `course_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xc_course_tables
-- ----------------------------
INSERT INTO `xc_course_tables` VALUES (5, 11, '52', 117, 1232141425, 'Nacos微服务开发实战', '700002', '2022-10-05 06:39:05', '2022-10-04 19:25:48', '2023-10-04 19:25:48', NULL, NULL);
INSERT INTO `xc_course_tables` VALUES (6, 12, '52', 121, 323232, 'Java编程思想', '700001', '2022-10-06 11:29:46', '2022-09-01 11:29:49', '2022-10-05 11:29:55', NULL, NULL);
INSERT INTO `xc_course_tables` VALUES (8, 12, '52', 124, 1232141425, '313免费课程测试', '700001', '2023-03-13 21:55:49', '2023-03-13 21:55:49', '2024-03-12 21:55:49', '2023-03-13 21:55:54', NULL);
INSERT INTO `xc_course_tables` VALUES (10, 14, '52', 126, 1232141425, '320收费课程测试', '700002', '2023-03-20 21:38:38', '2023-03-20 21:38:38', '2024-03-19 21:38:38', '2023-03-22 00:45:04', NULL);

-- ----------------------------
-- Table structure for xc_learn_record
-- ----------------------------
DROP TABLE IF EXISTS `xc_learn_record`;
CREATE TABLE `xc_learn_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程id',
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程名称',
  `user_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户id',
  `learn_date` datetime NULL DEFAULT NULL COMMENT '最近学习时间',
  `learn_length` bigint NULL DEFAULT NULL COMMENT '学习时长',
  `teachplan_id` bigint NULL DEFAULT NULL COMMENT '章节id',
  `teachplan_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '章节名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `learn_record_unique`(`course_id`, `user_id`, `teachplan_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xc_learn_record
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
