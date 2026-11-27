-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: dd
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `dd`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dd` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `dd`;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码(BCrypt加密存储)',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账号状态：1-正常 0-禁用',
  `token_version` bigint NOT NULL DEFAULT '0' COMMENT '令牌版本：改密/登出时自增使旧 token 失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='后台管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin','$2a$10$Hp7Z4ER9jIJIkuAmp/94.OU1Lt5j0.QdlM6qhMTdDb5hDn6CbK0Pm','判官',1,0,'2026-11-27 13:57:19','2026-11-27 13:57:19',0);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '主标题',
  `subtitle` varchar(160) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '副标题/描述',
  `tag` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '小标签(如上元灯会)',
  `accent` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主题强调色(HEX)，前端用于渐变光晕',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-展示 0-隐藏',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_banner_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='首页轮播标语/主题横幅';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (1,'月夜为媒，红妆为信','以三生石上旧精魂，许你一段不灭姻缘。','上元灯会','#E84A78',1,1,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(2,'十里红妆，铺作星河','胭脂钗环、凤冠霞帔，皆待有心人逐件拣选。','红妆阁','#F4D58B',2,1,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(3,'忘川彼岸，花叶不相见','纵身死而名相守，青石双碑刻两世同心。','往生簿','#9B6BD6',3,1,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(4,'月老执笔，红线系足','千里姻缘一线牵，今夜便在指尖成约。','姻缘簿','#FF6F9C',4,1,'2026-11-27 13:57:02','2026-11-27 13:57:02',0);
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID：0 表示一级分类',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '分类层级：1-一级 2-二级',
  `icon` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标(emoji)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分类描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表（支持多级：父分类->子分类）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,0,'姻缘簿',1,'💞',1,'天下姻缘尽录于此','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(2,0,'花轿信使',1,'📜',2,'往来阴阳的传书与请柬','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(3,0,'红妆阁',1,'🌹',3,'胭脂钗环与嫁衣镜奁','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(4,0,'往生簿',1,'🕯️',4,'生死两隔之恋','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(5,1,'前世姻缘',2,'💞',1,'三生石上旧精魂','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(6,1,'今生良缘',2,'💍',2,'合卺交杯结同心','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(7,1,'月老题名',2,'🌙',3,'朱笔一点定良缘','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(8,2,'红妆信物',2,'🎁',1,'金缕珠翠为盟','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(9,2,'花轿请柬',2,'✉️',2,'吉日良辰邀观礼','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(10,3,'嫁衣',2,'👰',1,'一袭红裳托终身','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(11,3,'胭脂',2,'💄',2,'桃花颜色点腮边','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(12,4,'往生姻缘',2,'🪷',1,'彼岸花开引归途','2026-11-27 13:57:02','2026-11-27 13:57:02',0),(13,4,'碑铭',2,'🪦',2,'青石双碑守名讳','2026-11-27 13:57:02','2026-11-27 13:57:02',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_comment`
--

DROP TABLE IF EXISTS `dating_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `content` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dating_comment_work` (`work_id`,`id`),
  KEY `idx_dating_comment_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作品评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_comment`
--

LOCK TABLES `dating_comment` WRITE;
/*!40000 ALTER TABLE `dating_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_contact`
--

DROP TABLE IF EXISTS `dating_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wechat` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信号',
  `qq` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'QQ号',
  `phone` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '交友手机号',
  `other` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '其他联系方式',
  `visible` tinyint NOT NULL DEFAULT '0' COMMENT '0仅私信过来往的人可见 1所有登录用户可见',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dating_contact_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='姻缘阁联系方式';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_contact`
--

LOCK TABLES `dating_contact` WRITE;
/*!40000 ALTER TABLE `dating_contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_like`
--

DROP TABLE IF EXISTS `dating_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dating_like` (`work_id`,`user_id`),
  KEY `idx_dating_like_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作品点赞';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_like`
--

LOCK TABLES `dating_like` WRITE;
/*!40000 ALTER TABLE `dating_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_message`
--

DROP TABLE IF EXISTS `dating_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者',
  `to_user_id` bigint NOT NULL COMMENT '接收者',
  `content` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '正文',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '1已读 0未读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dating_msg_to` (`to_user_id`,`from_user_id`,`id`),
  KEY `idx_dating_msg_from` (`from_user_id`,`to_user_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内私信';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_message`
--

LOCK TABLES `dating_message` WRITE;
/*!40000 ALTER TABLE `dating_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_work`
--

DROP TABLE IF EXISTS `dating_work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_work` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '作者用户ID',
  `title` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '作品标题',
  `content` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作品文案',
  `cover` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '封面地址',
  `bgm_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'BGM地址',
  `bgm_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'BGM名称',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '留言数',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
  `visible` tinyint NOT NULL DEFAULT '1' COMMENT '1公开 0仅自己可见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dating_work_user` (`user_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='姻缘阁作品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_work`
--

LOCK TABLES `dating_work` WRITE;
/*!40000 ALTER TABLE `dating_work` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_work` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dating_work_media`
--

DROP TABLE IF EXISTS `dating_work_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dating_work_media` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `media_type` varchar(16) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'image' COMMENT 'image/video',
  `url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '访问地址',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '顺序',
  PRIMARY KEY (`id`),
  KEY `idx_dating_media_work` (`work_id`,`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='姻缘阁作品媒体';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dating_work_media`
--

LOCK TABLES `dating_work_media` WRITE;
/*!40000 ALTER TABLE `dating_work_media` DISABLE KEYS */;
/*!40000 ALTER TABLE `dating_work_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `item_id` bigint NOT NULL COMMENT '条目ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_item` (`user_id`,`item_id`),
  KEY `idx_favorite_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL DEFAULT '0' COMMENT '所属分类ID',
  `title` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '描述',
  `emoji` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标(emoji)',
  `tag` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标签',
  `content` text COLLATE utf8mb4_general_ci COMMENT '正文/详情',
  `author` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '作者/来源',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-上线 0-下线',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '浏览次数（详情页每次打开 +1）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_item_category` (`category_id`),
  KEY `idx_item_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='条目/藏品表（可被搜索与首页展示）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (1,5,'月老的红线','月老以红线系足，千里姻缘一线牵。','🧵','月老','相传月老执掌天下姻缘簿，以红线系于有缘人足踝，虽远隔千山亦终成眷属。','月老',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(2,5,'三生石上的名字','三生石上旧精魂，缘定三生不可移。','🪨','姻缘簿','三生石立于忘川畔，刻有情人前世今生来世之名，纵经轮回亦不改其缘。','月老',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(3,6,'合卺酒','新人交杯合卺，自此同甘共苦。','🍶','红妆','合卺酒者，破瓠为二瓢，夫妇各执一瓢而饮，象征合二为一、永结同心。','花娘',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(4,6,'同心结','以丝绾同心，恩爱两不疑。','🎀','姻缘簿','同心结以锦缕双双编织，赠予新人以为信物，喻心心相印、白首不离。','花娘',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(5,7,'月老题名簿','姻缘簿上朱笔一点，良缘就此落定。','📖','月老','月老题名簿录尽凡尘有缘之人，朱笔一圈，便注定三生石上同姓之名。','月老',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(6,8,'金步摇','步摇金缕，行步则动摇生姿。','💛','红妆','金步摇为女子首饰，缀以金叶珠翠，行走时轻轻摇曳，添几分娇怯之态。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(7,8,'龙凤镯','一对龙凤，鎏金为盟。','💫','红妆','龙凤镯成双成对，男戴龙女戴凤，乃缔结婚约之重器，喻成双入对。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(8,9,'花轿请柬','大红请柬一封，邀君共赴花轿。','✉️','花轿','花轿请柬书明吉日良辰，遍邀亲友观礼，红纸金笺，喜气盈门。','花轿信使',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(9,9,'十里红妆单','十里红妆，铺陈一路繁华。','📜','红妆','十里红妆单详列嫁奁百物，自箱笼镜奁至被褥器皿，极尽人间富贵与祝愿。','花轿信使',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(10,10,'凤冠霞帔','凤冠加首，霞帔披身，此生为正室。','👑','红妆','凤冠霞帔为命妇与大婚正礼之服，珠翠累面、云霞满肩，极尽华贵庄重。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(11,10,'嫁衣','一袭嫁衣，缝尽阿娘千针万线。','👰','红妆','嫁衣以大红绸缎裁成，针针线线皆含娘家祝福，穿之于身便是一生托付。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(12,11,'胭脂盒','一点胭脂，染就桃花颜色。','💄','红妆','胭脂盒盛红粉，轻点腮边，便如三月桃花初绽，是新娘妆奁中不可或缺的娇物。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(13,12,'往生花','生于忘川彼岸，花叶永不相见。','🪷','往生','往生花（曼珠沙华）开于黄泉路旁，指引亡魂归往生途，亦喻生死两隔之恋。','忘川',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(14,12,'忘川渡','一叶扁舟，渡尽前尘旧事。','🌊','往生','忘川河横亘阴阳，摆渡人载亡魂过河，饮下孟汤，前缘尽忘，唯余此岸花红。','忘川',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(15,13,'碑上姻缘','青石为碑，刻一双不灭之名。','🪦','往生','碑上姻缘者，为早逝鸳鸯立双碑于一处，纵身死而名相守，世世可寻。','往生簿',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(16,1,'鬼娘花嫁总录','集天下鬼娘花嫁之姻缘于一册。','💞','姻缘簿','鬼娘花嫁总录汇录诸般冥婚典故与良缘，供有缘夫君于此寻觅命定之契。','月老',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(17,3,'红妆合集','红妆阁珍藏，件件皆是心头好。','🌹','红妆','红妆合集收尽胭脂钗环、嫁衣镜奁，一览红妆阁之盛，待新人逐件拣选。','红妆阁',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0),(18,2,'花轿信使录','信使奔走阴阳，递送相思。','📜','花轿','花轿信使录记往来阴阳之传书与请柬，纵隔幽明，亦可互通款曲、再缔良约。','花轿信使',1,0,'2026-11-27 13:57:02','2026-11-27 13:57:02',0);
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `memo`
--

DROP TABLE IF EXISTS `memo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `memo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID（由 JWT 解析，前端不传）',
  `title` varchar(120) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '标题',
  `content` text COLLATE utf8mb4_general_ci COMMENT '正文',
  `done` tinyint NOT NULL DEFAULT '0' COMMENT '是否完成：0-待办 1-已完成',
  `pinned` tinyint NOT NULL DEFAULT '0' COMMENT '是否置顶：0-否 1-是',
  `color` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'violet' COMMENT '便签配色：violet/pink/gold/mint',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_memo_user` (`user_id`),
  KEY `idx_memo_user_order` (`user_id`,`pinned`,`done`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户备忘录（按登录用户隔离）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `memo`
--

LOCK TABLES `memo` WRITE;
/*!40000 ALTER TABLE `memo` DISABLE KEYS */;
/*!40000 ALTER TABLE `memo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '夫君名讳/登录用户名',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '结契暗语/密码(BCrypt加密存储)',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像地址',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别：0-未知 1-男 2-女',
  `bio` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '个性签名/简介',
  `token_version` bigint NOT NULL DEFAULT '0' COMMENT '令牌版本：改密/登出时自增使旧 token 失效',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账号状态：1-正常 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表（鬼娘花嫁注册用户）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'hh','$2a$10$n9XqAfBbHSNsU.5b6o2X2eSn5WI1yCF/Std80hcgykUvxQQT7u1ce','hh',NULL,NULL,NULL,0,NULL,0,1,'2026-11-27 13:57:55','2026-11-27 13:57:55',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'dd'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-27 14:04:18
