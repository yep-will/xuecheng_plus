/*
 Navicat MySQL Data Transfer

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : xc_media

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 04/04/2023 09:08:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for media_files
-- ----------------------------
DROP TABLE IF EXISTS `media_files`;
CREATE TABLE `media_files`  (
  `id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件id,md5值',
  `company_id` bigint NULL DEFAULT NULL COMMENT '机构ID',
  `company_name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '机构名称',
  `filename` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件名称',
  `file_type` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件类型（图片、文档，视频）',
  `tags` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '标签',
  `bucket` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储目录',
  `file_path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储路径',
  `file_id` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件id',
  `url` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '媒资文件访问地址',
  `username` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '上传人',
  `create_date` datetime NULL DEFAULT NULL COMMENT '上传时间',
  `change_date` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `status` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '1' COMMENT '状态,1:正常，0:不展示',
  `remark` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '备注',
  `audit_status` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '审核状态',
  `audit_mind` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_fileid`(`file_id`) USING BTREE COMMENT '文件id唯一索引 '
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '媒资信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of media_files
-- ----------------------------
INSERT INTO `media_files` VALUES ('1ae5ac1709c869affedc134ee446434e', 1232141425, NULL, 'course7767610388456726127.html', '001003', NULL, 'mediafiles', 'course/126.html', '1ae5ac1709c869affedc134ee446434e', NULL, NULL, '2023-03-20 21:34:50', NULL, '1', NULL, '002003', NULL, 34350);
INSERT INTO `media_files` VALUES ('1fb1c00103e5bbf73ace22db2818ce09', 1232141425, NULL, 'course7448715627857721530.html', '001003', NULL, 'mediafiles', 'course/124.html', '1fb1c00103e5bbf73ace22db2818ce09', NULL, NULL, '2023-03-13 21:34:13', NULL, '1', NULL, '002003', NULL, 39559);
INSERT INTO `media_files` VALUES ('2942ac69e25a54fb6539ee27bfbdc907', 1232141425, NULL, 'xiaomi.mp4', '001002', '课程视频', 'video', '2/9/2942ac69e25a54fb6539ee27bfbdc907/2942ac69e25a54fb6539ee27bfbdc907.avi', '2942ac69e25a54fb6539ee27bfbdc907', '/video/2/9/2942ac69e25a54fb6539ee27bfbdc907/2942ac69e25a54fb6539ee27bfbdc907.mp4', NULL, '2023-02-25 19:59:07', NULL, '1', NULL, '002003', NULL, 1583121);
INSERT INTO `media_files` VALUES ('426adc24cf9bd7e33e5d6934ae53c791', 1232141425, NULL, '旧厂街.mp4', '001002', '课程视频', 'video', '4/2/426adc24cf9bd7e33e5d6934ae53c791/426adc24cf9bd7e33e5d6934ae53c791.avi', '426adc24cf9bd7e33e5d6934ae53c791', '/video/4/2/426adc24cf9bd7e33e5d6934ae53c791/426adc24cf9bd7e33e5d6934ae53c791.mp4', NULL, '2023-02-25 19:59:18', NULL, '1', NULL, '002003', NULL, 3604371);
INSERT INTO `media_files` VALUES ('4be0a10e5c71770e9d3be87c3ebccf19', 1232141425, NULL, 'messi.mp4', '001002', '课程视频', 'video', '4/b/4be0a10e5c71770e9d3be87c3ebccf19/4be0a10e5c71770e9d3be87c3ebccf19.avi', '4be0a10e5c71770e9d3be87c3ebccf19', '/video/4/b/4be0a10e5c71770e9d3be87c3ebccf19/4be0a10e5c71770e9d3be87c3ebccf19.mp4', NULL, '2023-02-25 19:59:08', NULL, '1', NULL, '002003', NULL, 6358653);
INSERT INTO `media_files` VALUES ('4e519b0d19df13ad02953e1d43429876', 1232141425, NULL, '斯柯达.mp4', '001002', '课程视频', 'video', '4/e/4e519b0d19df13ad02953e1d43429876/4e519b0d19df13ad02953e1d43429876.avi', '4e519b0d19df13ad02953e1d43429876', '/video/4/e/4e519b0d19df13ad02953e1d43429876/4e519b0d19df13ad02953e1d43429876.mp4', NULL, '2023-02-25 19:59:20', NULL, '1', NULL, '002003', NULL, 3901665);
INSERT INTO `media_files` VALUES ('7a342495ad4bcce838bddf410b2fd03d', 1232141425, NULL, 'course2184630797047153191.html', '001003', NULL, 'mediafiles', 'course/123.html', '7a342495ad4bcce838bddf410b2fd03d', NULL, NULL, '2023-03-13 21:34:13', NULL, '1', NULL, '002003', NULL, 39889);
INSERT INTO `media_files` VALUES ('87bdce9224062fb268c5ddc9d25a8b3b', 1232141425, NULL, '壁纸3.jpg', '001001', NULL, 'mediafiles', '2023/03/04/87bdce9224062fb268c5ddc9d25a8b3b.jpg', '87bdce9224062fb268c5ddc9d25a8b3b', '/mediafiles/2023/03/04/87bdce9224062fb268c5ddc9d25a8b3b.jpg', NULL, '2023-03-04 00:05:40', NULL, '1', NULL, '002003', NULL, 766037);
INSERT INTO `media_files` VALUES ('8ac97da2b868df9ced54d4933f188982', 1232141425, NULL, '莫妮卡.jpg', '001001', NULL, 'mediafiles', '2023/03/03/8ac97da2b868df9ced54d4933f188982.jpg', '8ac97da2b868df9ced54d4933f188982', '/mediafiles/2023/03/03/8ac97da2b868df9ced54d4933f188982.jpg', NULL, '2023-03-03 15:53:30', NULL, '1', NULL, '002003', NULL, 146536);
INSERT INTO `media_files` VALUES ('abab52a88200da1f4382b422472899ac', 1232141425, NULL, '高铁.mp4', '001002', '课程视频', 'video', 'a/b/abab52a88200da1f4382b422472899ac/abab52a88200da1f4382b422472899ac.avi', 'abab52a88200da1f4382b422472899ac', '/video/a/b/abab52a88200da1f4382b422472899ac/abab52a88200da1f4382b422472899ac.mp4', NULL, '2023-02-25 19:59:14', NULL, '1', NULL, '002003', NULL, 2082203);
INSERT INTO `media_files` VALUES ('b93d72d4652f1a108db3d5865532fc5e', 1232141425, NULL, '刘亦菲.jpg', '001001', NULL, 'mediafiles', '2023/03/20/b93d72d4652f1a108db3d5865532fc5e.jpg', 'b93d72d4652f1a108db3d5865532fc5e', '/mediafiles/2023/03/20/b93d72d4652f1a108db3d5865532fc5e.jpg', NULL, '2023-03-20 21:02:36', NULL, '1', NULL, '002003', NULL, 351709);
INSERT INTO `media_files` VALUES ('c1869715a8a06208222d8b6482f1d20d', 1232141425, NULL, 'course2079244646398981475.html', '001003', NULL, 'mediafiles', 'course/124.html', 'c1869715a8a06208222d8b6482f1d20d', NULL, NULL, '2023-03-04 00:10:28', NULL, '1', NULL, '002003', NULL, 38942);
INSERT INTO `media_files` VALUES ('cd53826b7d95dc827745c9545c5342e7', 1232141425, NULL, '瑞秋.jpg', '001001', NULL, 'mediafiles', '2023/02/25/cd53826b7d95dc827745c9545c5342e7.jpg', 'cd53826b7d95dc827745c9545c5342e7', '/mediafiles/2023/02/25/cd53826b7d95dc827745c9545c5342e7.jpg', NULL, '2023-02-25 20:04:30', NULL, '1', NULL, '002003', NULL, 173626);
INSERT INTO `media_files` VALUES ('d82a2ce21f92d589117dbd27d8b0e0b8', 1232141425, NULL, 'course7830937450551407836.html', '001003', NULL, 'mediafiles', 'course/123.html', 'd82a2ce21f92d589117dbd27d8b0e0b8', NULL, NULL, '2023-03-03 16:23:55', NULL, '1', NULL, '002003', NULL, 39163);
INSERT INTO `media_files` VALUES ('dea0a28ceed17e974040dc1dced51ee6', 1232141425, NULL, '艾克森.mp4', '001002', '课程视频', 'video', 'd/e/dea0a28ceed17e974040dc1dced51ee6/dea0a28ceed17e974040dc1dced51ee6.avi', 'dea0a28ceed17e974040dc1dced51ee6', '/video/d/e/dea0a28ceed17e974040dc1dced51ee6/dea0a28ceed17e974040dc1dced51ee6.mp4', NULL, '2023-02-25 19:59:11', NULL, '1', NULL, '002003', NULL, 4855211);

-- ----------------------------
-- Table structure for media_process
-- ----------------------------
DROP TABLE IF EXISTS `media_process`;
CREATE TABLE `media_process`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_id` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件标识',
  `filename` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件名称',
  `bucket` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '存储桶',
  `file_path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '存储路径',
  `status` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '状态,1:未处理，2：处理成功  3处理失败',
  `create_date` datetime NOT NULL COMMENT '上传时间',
  `finish_date` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `url` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '媒资文件访问地址',
  `errormsg` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '失败原因',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_fileid`(`file_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of media_process
-- ----------------------------

-- ----------------------------
-- Table structure for media_process_history
-- ----------------------------
DROP TABLE IF EXISTS `media_process_history`;
CREATE TABLE `media_process_history`  (
  `id` bigint NOT NULL,
  `file_id` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件标识',
  `filename` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件名称',
  `bucket` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '存储源',
  `status` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '状态,1:未处理，2：处理成功  3处理失败',
  `create_date` datetime NOT NULL COMMENT '上传时间',
  `finish_date` datetime NOT NULL COMMENT '完成时间',
  `url` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '媒资文件访问地址',
  `file_path` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `errormsg` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of media_process_history
-- ----------------------------
INSERT INTO `media_process_history` VALUES (17, '2942ac69e25a54fb6539ee27bfbdc907', 'xiaomi.mp4', 'video', '2', '2023-02-25 19:59:07', '2023-02-25 19:59:44', '/video/2/9/2942ac69e25a54fb6539ee27bfbdc907/2942ac69e25a54fb6539ee27bfbdc907.mp4', NULL, NULL);
INSERT INTO `media_process_history` VALUES (18, '4be0a10e5c71770e9d3be87c3ebccf19', 'messi.mp4', 'video', '2', '2023-02-25 19:59:08', '2023-02-25 19:59:54', '/video/4/b/4be0a10e5c71770e9d3be87c3ebccf19/4be0a10e5c71770e9d3be87c3ebccf19.mp4', NULL, NULL);
INSERT INTO `media_process_history` VALUES (19, 'dea0a28ceed17e974040dc1dced51ee6', '艾克森.mp4', 'video', '2', '2023-02-25 19:59:11', '2023-02-25 19:59:50', '/video/d/e/dea0a28ceed17e974040dc1dced51ee6/dea0a28ceed17e974040dc1dced51ee6.mp4', NULL, NULL);
INSERT INTO `media_process_history` VALUES (20, 'abab52a88200da1f4382b422472899ac', '高铁.mp4', 'video', '2', '2023-02-25 19:59:14', '2023-02-25 19:59:46', '/video/a/b/abab52a88200da1f4382b422472899ac/abab52a88200da1f4382b422472899ac.mp4', NULL, NULL);
INSERT INTO `media_process_history` VALUES (21, '426adc24cf9bd7e33e5d6934ae53c791', '旧厂街.mp4', 'video', '2', '2023-02-25 19:59:18', '2023-02-25 20:00:01', '/video/4/2/426adc24cf9bd7e33e5d6934ae53c791/426adc24cf9bd7e33e5d6934ae53c791.mp4', NULL, NULL);
INSERT INTO `media_process_history` VALUES (22, '4e519b0d19df13ad02953e1d43429876', '斯柯达.mp4', 'video', '2', '2023-02-25 19:59:20', '2023-02-25 20:00:01', '/video/4/e/4e519b0d19df13ad02953e1d43429876/4e519b0d19df13ad02953e1d43429876.mp4', NULL, NULL);

-- ----------------------------
-- Table structure for mq_message
-- ----------------------------
DROP TABLE IF EXISTS `mq_message`;
CREATE TABLE `mq_message`  (
  `id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息id',
  `message_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息类型代码',
  `business_key1` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key2` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key3` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `mq_host` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息队列主机',
  `mq_port` int NOT NULL COMMENT '消息队列端口',
  `mq_virtualhost` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息队列虚拟主机',
  `mq_queue` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '队列名称',
  `inform_num` int UNSIGNED NOT NULL COMMENT '通知次数',
  `state` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '处理状态，0:初始，1:成功',
  `returnfailure_date` datetime NULL DEFAULT NULL COMMENT '回复失败时间',
  `returnsuccess_date` datetime NULL DEFAULT NULL COMMENT '回复成功时间',
  `returnfailure_msg` varchar(2048) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '回复失败内容',
  `inform_date` datetime NULL DEFAULT NULL COMMENT '最近通知时间',
  `stage_state1` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '阶段1处理状态, 0:初始，1:成功',
  `stage_state2` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '阶段2处理状态, 0:初始，1:成功',
  `stage_state3` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '阶段3处理状态, 0:初始，1:成功',
  `stage_state4` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '阶段4处理状态, 0:初始，1:成功',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mq_message
-- ----------------------------
INSERT INTO `mq_message` VALUES ('f29a3149-7429-40be-8a4e-9909f32003b0', 'xc.mq.msgsync.coursepub', '111', NULL, NULL, '127.0.0.1', 5607, '/', 'xc.course.publish.queue', 0, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for mq_message_history
-- ----------------------------
DROP TABLE IF EXISTS `mq_message_history`;
CREATE TABLE `mq_message_history`  (
  `id` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息id',
  `message_type` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息类型代码',
  `business_key1` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key2` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key3` varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `mq_host` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息队列主机',
  `mq_port` int NOT NULL COMMENT '消息队列端口',
  `mq_virtualhost` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '消息队列虚拟主机',
  `mq_queue` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '队列名称',
  `inform_num` int(10) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '通知次数',
  `state` int(10) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '处理状态，0:初始，1:成功，2:失败',
  `returnfailure_date` datetime NULL DEFAULT NULL COMMENT '回复失败时间',
  `returnsuccess_date` datetime NULL DEFAULT NULL COMMENT '回复成功时间',
  `returnfailure_msg` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '回复失败内容',
  `inform_date` datetime NULL DEFAULT NULL COMMENT '最近通知时间',
  `stage_state1` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `stage_state2` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `stage_state3` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `stage_state4` char(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mq_message_history
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
