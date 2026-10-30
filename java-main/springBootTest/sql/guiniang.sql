/*
 Navicat Premium Dump SQL

 Source Server         : xx
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : guiniang

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 30/10/2026 09:44:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码(BCrypt加密存储)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '后台管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$sFLDNecGREQJq/0qboF/lutWL2BMdUAzGYtznJxrSQXbkhqowxgQO', '判官', 1, '2026-10-30 09:38:09', '2026-10-30 09:38:09', 0);

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主标题',
  `subtitle` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题/描述',
  `tag` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '小标签(如上元灯会)',
  `accent` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主题强调色(HEX)，前端用于渐变光晕',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-展示 0-隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_banner_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '首页轮播标语/主题横幅' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '月夜为媒，红妆为信', '以三生石上旧精魂，许你一段不灭姻缘。', '上元灯会', '#E84A78', 1, 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `banner` VALUES (2, '十里红妆，铺作星河', '胭脂钗环、凤冠霞帔，皆待有心人逐件拣选。', '红妆阁', '#F4D58B', 2, 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `banner` VALUES (3, '忘川彼岸，花叶不相见', '纵身死而名相守，青石双碑刻两世同心。', '往生簿', '#9B6BD6', 3, 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `banner` VALUES (4, '月老执笔，红线系足', '千里姻缘一线牵，今夜便在指尖成约。', '姻缘簿', '#FF6F9C', 4, 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID：0 表示一级分类',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级 2-二级',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标(emoji)',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分类表（支持多级：父分类->子分类）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, 0, '姻缘簿', 1, '💞', 1, '天下姻缘尽录于此', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (2, 0, '花轿信使', 1, '📜', 2, '往来阴阳的传书与请柬', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (3, 0, '红妆阁', 1, '🌹', 3, '胭脂钗环与嫁衣镜奁', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (4, 0, '往生簿', 1, '🕯️', 4, '生死两隔之恋', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (5, 1, '前世姻缘', 2, '💞', 1, '三生石上旧精魂', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (6, 1, '今生良缘', 2, '💍', 2, '合卺交杯结同心', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (7, 1, '月老题名', 2, '🌙', 3, '朱笔一点定良缘', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (8, 2, '红妆信物', 2, '🎁', 1, '金缕珠翠为盟', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (9, 2, '花轿请柬', 2, '✉️', 2, '吉日良辰邀观礼', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (10, 3, '嫁衣', 2, '👰', 1, '一袭红裳托终身', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (11, 3, '胭脂', 2, '💄', 2, '桃花颜色点腮边', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (12, 4, '往生姻缘', 2, '🪷', 1, '彼岸花开引归途', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `category` VALUES (13, 4, '碑铭', 2, '🪦', 2, '青石双碑守名讳', '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL DEFAULT 0 COMMENT '所属分类ID',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `emoji` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标(emoji)',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '正文/详情',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者/来源',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-上线 0-下线',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_item_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '条目/藏品表（可被搜索与首页展示）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES (1, 5, '月老的红线', '月老以红线系足，千里姻缘一线牵。', '🧵', '月老', '相传月老执掌天下姻缘簿，以红线系于有缘人足踝，虽远隔千山亦终成眷属。', '月老', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (2, 5, '三生石上的名字', '三生石上旧精魂，缘定三生不可移。', '🪨', '姻缘簿', '三生石立于忘川畔，刻有情人前世今生来世之名，纵经轮回亦不改其缘。', '月老', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (3, 6, '合卺酒', '新人交杯合卺，自此同甘共苦。', '🍶', '红妆', '合卺酒者，破瓠为二瓢，夫妇各执一瓢而饮，象征合二为一、永结同心。', '花娘', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (4, 6, '同心结', '以丝绾同心，恩爱两不疑。', '🎀', '姻缘簿', '同心结以锦缕双双编织，赠予新人以为信物，喻心心相印、白首不离。', '花娘', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (5, 7, '月老题名簿', '姻缘簿上朱笔一点，良缘就此落定。', '📖', '月老', '月老题名簿录尽凡尘有缘之人，朱笔一圈，便注定三生石上同姓之名。', '月老', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (6, 8, '金步摇', '步摇金缕，行步则动摇生姿。', '💛', '红妆', '金步摇为女子首饰，缀以金叶珠翠，行走时轻轻摇曳，添几分娇怯之态。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (7, 8, '龙凤镯', '一对龙凤，鎏金为盟。', '💫', '红妆', '龙凤镯成双成对，男戴龙女戴凤，乃缔结婚约之重器，喻成双入对。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (8, 9, '花轿请柬', '大红请柬一封，邀君共赴花轿。', '✉️', '花轿', '花轿请柬书明吉日良辰，遍邀亲友观礼，红纸金笺，喜气盈门。', '花轿信使', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (9, 9, '十里红妆单', '十里红妆，铺陈一路繁华。', '📜', '红妆', '十里红妆单详列嫁奁百物，自箱笼镜奁至被褥器皿，极尽人间富贵与祝愿。', '花轿信使', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (10, 10, '凤冠霞帔', '凤冠加首，霞帔披身，此生为正室。', '👑', '红妆', '凤冠霞帔为命妇与大婚正礼之服，珠翠累面、云霞满肩，极尽华贵庄重。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (11, 10, '嫁衣', '一袭嫁衣，缝尽阿娘千针万线。', '👰', '红妆', '嫁衣以大红绸缎裁成，针针线线皆含娘家祝福，穿之于身便是一生托付。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (12, 11, '胭脂盒', '一点胭脂，染就桃花颜色。', '💄', '红妆', '胭脂盒盛红粉，轻点腮边，便如三月桃花初绽，是新娘妆奁中不可或缺的娇物。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (13, 12, '往生花', '生于忘川彼岸，花叶永不相见。', '🪷', '往生', '往生花（曼珠沙华）开于黄泉路旁，指引亡魂归往生途，亦喻生死两隔之恋。', '忘川', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (14, 12, '忘川渡', '一叶扁舟，渡尽前尘旧事。', '🌊', '往生', '忘川河横亘阴阳，摆渡人载亡魂过河，饮下孟汤，前缘尽忘，唯余此岸花红。', '忘川', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (15, 13, '碑上姻缘', '青石为碑，刻一双不灭之名。', '🪦', '往生', '碑上姻缘者，为早逝鸳鸯立双碑于一处，纵身死而名相守，世世可寻。', '往生簿', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (16, 1, '鬼娘花嫁总录', '集天下鬼娘花嫁之姻缘于一册。', '💞', '姻缘簿', '鬼娘花嫁总录汇录诸般冥婚典故与良缘，供有缘夫君于此寻觅命定之契。', '月老', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (17, 3, '红妆合集', '红妆阁珍藏，件件皆是心头好。', '🌹', '红妆', '红妆合集收尽胭脂钗环、嫁衣镜奁，一览红妆阁之盛，待新人逐件拣选。', '红妆阁', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);
INSERT INTO `item` VALUES (18, 2, '花轿信使录', '信使奔走阴阳，递送相思。', '📜', '花轿', '花轿信使录记往来阴阳之传书与请柬，纵隔幽明，亦可互通款曲、再缔良约。', '花轿信使', 1, '2026-10-30 09:38:08', '2026-10-30 09:38:08', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '夫君名讳/登录用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结契暗语/密码(BCrypt加密存储)',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表（鬼娘花嫁注册用户）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
