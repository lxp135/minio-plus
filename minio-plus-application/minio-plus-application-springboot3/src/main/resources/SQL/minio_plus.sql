/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50729 (5.7.29-log)
 Source Host           : localhost:3306
 Source Schema         : minio_plus

 Target Server Type    : MySQL
 Target Server Version : 50729 (5.7.29-log)
 File Encoding         : 65001

 Date: 03/06/2024 13:36:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for file_metadata_info
-- ----------------------------
DROP TABLE IF EXISTS `file_metadata_info`;
CREATE TABLE `file_metadata_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件KEY',
  `file_md5` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件md5',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件名',
  `file_mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `file_suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件后缀',
  `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
  `is_preview` tinyint(1) NULL DEFAULT 0 COMMENT '预览图 0:无 1:有',
  `is_private` tinyint(1) NULL DEFAULT 0 COMMENT '是否私有 0:否 1:是',
  `bucket` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储桶',
  `bucket_path` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储桶路径',
  `upload_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '上传任务id',
  `is_finished` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态 0:未完成 1:已完成',
  `is_part` tinyint(1) NULL DEFAULT NULL COMMENT '是否分块 0:否 1:是',
  `part_number` int(4) NULL DEFAULT NULL COMMENT '分块数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '更新用户',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `INDEX_KEY`(`file_key`) USING BTREE,
  INDEX `INDEX_MD5`(`file_md5`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件元数据信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
