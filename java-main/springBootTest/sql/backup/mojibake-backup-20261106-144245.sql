-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: dd
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
-- Table structure for table `item`
--

DROP TABLE IF EXISTS `item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `category_id` bigint NOT NULL DEFAULT '0' COMMENT '鎵?睘鍒嗙被ID',
  `title` varchar(120) COLLATE utf8mb4_general_ci NOT NULL COMMENT '鏍囬?',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鎻忚堪',
  `emoji` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍥炬爣(emoji)',
  `tag` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鏍囩?',
  `content` text COLLATE utf8mb4_general_ci COMMENT '姝ｆ枃/璇︽儏',
  `author` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '浣滆?/鏉ユ簮',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '鐘舵?锛?-涓婄嚎 0-涓嬬嚎',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '娴忚?娆℃暟锛堣?鎯呴〉姣忔?鎵撳紑 +1锛',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '閫昏緫鍒犻櫎锛?-鏈?垹闄?1-宸插垹闄',
  PRIMARY KEY (`id`),
  KEY `idx_item_category` (`category_id`),
  KEY `idx_item_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鏉＄洰/钘忓搧琛?紙鍙??鎼滅储涓庨?椤靛睍绀猴級';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `item`
--

LOCK TABLES `item` WRITE;
/*!40000 ALTER TABLE `item` DISABLE KEYS */;
INSERT INTO `item` VALUES (1,5,'鏈堣?鐨勭孩绾','鏈堣?浠ョ孩绾跨郴瓒筹紝鍗冮噷濮荤紭涓?嚎鐗点?','馃У','鏈堣?','鐩镐紶鏈堣?鎵ф帉澶╀笅濮荤紭绨匡紝浠ョ孩绾跨郴浜庢湁缂樹汉瓒宠笣锛岃櫧杩滈殧鍗冨北浜︾粓鎴愮湻灞炪?','鏈堣?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(2,5,'涓夌敓鐭充笂鐨勫悕瀛','涓夌敓鐭充笂鏃х簿榄傦紝缂樺畾涓夌敓涓嶅彲绉汇?','馃?','濮荤紭绨','涓夌敓鐭崇珛浜庡繕宸濈晹锛屽埢鏈夋儏浜哄墠涓栦粖鐢熸潵涓栦箣鍚嶏紝绾电粡杞?洖浜︿笉鏀瑰叾缂樸?','鏈堣?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(3,6,'鍚堝嵑閰','鏂颁汉浜ゆ澂鍚堝嵑锛岃嚜姝ゅ悓鐢樺叡鑻︺?','馃嵍','绾㈠?','鍚堝嵑閰掕?锛岀牬鐡犱负浜岀摙锛屽か濡囧悇鎵т竴鐡㈣?楗?紝璞″緛鍚堜簩涓轰竴銆佹案缁撳悓蹇冦?','鑺卞?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(4,6,'鍚屽績缁','浠ヤ笣缁惧悓蹇冿紝鎭╃埍涓や笉鐤戙?','馃巰','濮荤紭绨','鍚屽績缁撲互閿︾紩鍙屽弻缂栫粐锛岃禒浜堟柊浜轰互涓轰俊鐗╋紝鍠诲績蹇冪浉鍗般?鐧介?涓嶇?銆','鑺卞?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(5,7,'鏈堣?棰樺悕绨','濮荤紭绨夸笂鏈辩瑪涓?偣锛岃壇缂樺氨姝よ惤瀹氥?','馃摉','鏈堣?','鏈堣?棰樺悕绨垮綍灏藉嚒灏樻湁缂樹箣浜猴紝鏈辩瑪涓?湀锛屼究娉ㄥ畾涓夌敓鐭充笂鍚屽?涔嬪悕銆','鏈堣?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(6,8,'閲戞?鎽','姝ユ憞閲戠紩锛岃?姝ュ垯鍔ㄦ憞鐢熷Э銆','馃挍','绾㈠?','閲戞?鎽囦负濂冲瓙棣栭グ锛岀紑浠ラ噾鍙剁彔缈狅紝琛岃蛋鏃惰交杞绘憞鏇筹紝娣诲嚑鍒嗗▏鎬?箣鎬併?','绾㈠?闃',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(7,8,'榫欏嚖闀','涓??榫欏嚖锛岄帍閲戜负鐩熴?','馃挮','绾㈠?','榫欏嚖闀?垚鍙屾垚瀵癸紝鐢锋埓榫欏コ鎴村嚖锛屼箖缂旂粨濠氱害涔嬮噸鍣?紝鍠绘垚鍙屽叆瀵广?','绾㈠?闃',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(8,9,'鑺辫娇璇锋煬','澶х孩璇锋煬涓?皝锛岄個鍚涘叡璧磋姳杞裤?','鉁夛笍','鑺辫娇','鑺辫娇璇锋煬涔︽槑鍚夋棩鑹?景锛岄亶閭?翰鍙嬭?绀硷紝绾㈢焊閲戠?锛屽枩姘旂泩闂ㄣ?','鑺辫娇淇′娇',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(9,9,'鍗侀噷绾㈠?鍗','鍗侀噷绾㈠?锛岄摵闄堜竴璺?箒鍗庛?','馃摐','绾㈠?','鍗侀噷绾㈠?鍗曡?鍒楀珌濂佺櫨鐗╋紝鑷??绗奸暅濂佽嚦琚?ぅ鍣ㄧ毧锛屾瀬灏戒汉闂村瘜璐典笌绁濇効銆','鑺辫娇淇′娇',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(10,10,'鍑ゅ啝闇炲笖','鍑ゅ啝鍔犻?锛岄湠甯旀姭韬?紝姝ょ敓涓烘?瀹ゃ?','馃憫','绾㈠?','鍑ゅ啝闇炲笖涓哄懡濡囦笌澶у?姝ｇぜ涔嬫湇锛岀彔缈犵疮闈??浜戦湠婊¤偐锛屾瀬灏藉崕璐靛簞閲嶃?','绾㈠?闃',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(11,10,'瀚佽。','涓??瀚佽。锛岀紳灏介樋濞樺崈閽堜竾绾裤?','馃懓','绾㈠?','瀚佽。浠ュぇ绾㈢桓缂庤?鎴愶紝閽堥拡绾跨嚎鐨嗗惈濞樺?绁濈?锛岀┛涔嬩簬韬?究鏄?竴鐢熸墭浠樸?','绾㈠?闃',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(12,11,'鑳?剛鐩','涓?偣鑳?剛锛屾煋灏辨?鑺遍?鑹层?','馃拕','绾㈠?','鑳?剛鐩掔洓绾㈢矇锛岃交鐐硅叜杈癸紝渚垮?涓夋湀妗冭姳鍒濈唤锛屾槸鏂板?濡嗗?涓?笉鍙?垨缂虹殑濞囩墿銆','绾㈠?闃',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(13,12,'寰?敓鑺','鐢熶簬蹇樺窛褰煎哺锛岃姳鍙舵案涓嶇浉瑙併?','馃?','寰?敓','寰?敓鑺憋紙鏇肩彔娌欏崕锛夊紑浜庨粍娉夎矾鏃侊紝鎸囧紩浜￠瓊褰掑線鐢熼?锛屼害鍠荤敓姝讳袱闅斾箣鎭嬨?','蹇樺窛',1,1,'2026-11-06 13:58:33','2026-11-06 14:01:26',0),(14,12,'蹇樺窛娓','涓?彾鎵佽垷锛屾浮灏藉墠灏樻棫浜嬨?','馃寠','寰?敓','蹇樺窛娌虫í浜橀槾闃筹紝鎽嗘浮浜鸿浇浜￠瓊杩囨渤锛岄ギ涓嬪瓱姹わ紝鍓嶇紭灏藉繕锛屽敮浣欐?宀歌姳绾??','蹇樺窛',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(15,13,'纰戜笂濮荤紭','闈掔煶涓虹?锛屽埢涓?弻涓嶇伃涔嬪悕銆','馃?','寰?敓','纰戜笂濮荤紭鑰咃紝涓烘棭閫濋赋楦?珛鍙岀?浜庝竴澶勶紝绾佃韩姝昏?鍚嶇浉瀹堬紝涓栦笘鍙??銆','寰?敓绨',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(16,1,'楝煎?鑺卞珌鎬诲綍','闆嗗ぉ涓嬮?濞樿姳瀚佷箣濮荤紭浜庝竴鍐屻?','馃挒','濮荤紭绨','楝煎?鑺卞珌鎬诲綍姹囧綍璇歌埇鍐ュ?鍏告晠涓庤壇缂橈紝渚涙湁缂樺か鍚涗簬姝ゅ?瑙呭懡瀹氫箣濂戙?','鏈堣?',1,0,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(17,3,'绾㈠?鍚堥泦','绾㈠?闃佺弽钘忥紝浠朵欢鐨嗘槸蹇冨ご濂姐?','馃尮','绾㈠?','绾㈠?鍚堥泦鏀跺敖鑳?剛閽楃幆銆佸珌琛ｉ暅濂侊紝涓??绾㈠?闃佷箣鐩涳紝寰呮柊浜洪?浠舵嫞閫夈?','绾㈠?闃',1,1,'2026-11-06 13:58:33','2026-11-06 14:04:16',0),(18,2,'鑺辫娇淇′娇褰','淇′娇濂旇蛋闃撮槼锛岄?閫佺浉鎬濄?','馃摐','鑺辫娇','鑺辫娇淇′娇褰曡?寰?潵闃撮槼涔嬩紶涔︿笌璇锋煬锛岀旱闅斿菇鏄庯紝浜﹀彲浜掗?娆炬洸銆佸啀缂旇壇绾︺?','鑺辫娇淇′娇',1,1,'2026-11-06 13:58:33','2026-11-06 14:21:24',0);
/*!40000 ALTER TABLE `item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '鐖跺垎绫籌D锛? 琛ㄧず涓?骇鍒嗙被',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '鍒嗙被鍚嶇О',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '鍒嗙被灞傜骇锛?-涓?骇 2-浜岀骇',
  `icon` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍥炬爣(emoji)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '鎺掑簭',
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍒嗙被鎻忚堪',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '閫昏緫鍒犻櫎锛?-鏈?垹闄?1-宸插垹闄',
  PRIMARY KEY (`id`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='鍒嗙被琛?紙鏀?寔澶氱骇锛氱埗鍒嗙被->瀛愬垎绫伙級';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,0,'濮荤紭绨',1,'馃挒',1,'澶╀笅濮荤紭灏藉綍浜庢?','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(2,0,'鑺辫娇淇′娇',1,'馃摐',2,'寰?潵闃撮槼鐨勪紶涔︿笌璇锋煬','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(3,0,'绾㈠?闃',1,'馃尮',3,'鑳?剛閽楃幆涓庡珌琛ｉ暅濂','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(4,0,'寰?敓绨',1,'馃暞锔',4,'鐢熸?涓ら殧涔嬫亱','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(5,1,'鍓嶄笘濮荤紭',2,'馃挒',1,'涓夌敓鐭充笂鏃х簿榄','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(6,1,'浠婄敓鑹?紭',2,'馃拲',2,'鍚堝嵑浜ゆ澂缁撳悓蹇','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(7,1,'鏈堣?棰樺悕',2,'馃寵',3,'鏈辩瑪涓?偣瀹氳壇缂','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(8,2,'绾㈠?淇＄墿',2,'馃巵',1,'閲戠紩鐝犵繝涓虹洘','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(9,2,'鑺辫娇璇锋煬',2,'鉁夛笍',2,'鍚夋棩鑹?景閭??绀','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(10,3,'瀚佽。',2,'馃懓',1,'涓??绾㈣３鎵樼粓韬','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(11,3,'鑳?剛',2,'馃拕',2,'妗冭姳棰滆壊鐐硅叜杈','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(12,4,'寰?敓濮荤紭',2,'馃?',1,'褰煎哺鑺卞紑寮曞綊閫','2026-11-06 13:58:33','2026-11-06 13:58:33',0),(13,4,'纰戦摥',2,'馃?',2,'闈掔煶鍙岀?瀹堝悕璁','2026-11-06 13:58:33','2026-11-06 13:58:33',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `title` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '涓绘爣棰',
  `subtitle` varchar(160) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '鍓?爣棰?鎻忚堪',
  `tag` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '灏忔爣绛?濡備笂鍏冪伅浼?',
  `accent` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '涓婚?寮鸿皟鑹?HEX)锛屽墠绔?敤浜庢笎鍙樺厜鏅',
  `sort` int NOT NULL DEFAULT '0' COMMENT '鎺掑簭',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '鐘舵?锛?-灞曠ず 0-闅愯棌',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '閫昏緫鍒犻櫎锛?-鏈?垹闄?1-宸插垹闄',
  PRIMARY KEY (`id`),
  KEY `idx_banner_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='棣栭〉杞?挱鏍囪?/涓婚?妯?箙';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (1,'鏈堝?涓哄獟锛岀孩濡嗕负淇','浠ヤ笁鐢熺煶涓婃棫绮鹃瓊锛岃?浣犱竴娈典笉鐏?Щ缂樸?','涓婂厓鐏?細','#E84A78',1,1,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(2,'鍗侀噷绾㈠?锛岄摵浣滄槦娌','鑳?剛閽楃幆銆佸嚖鍐犻湠甯旓紝鐨嗗緟鏈夊績浜洪?浠舵嫞閫夈?','绾㈠?闃','#F4D58B',2,1,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(3,'蹇樺窛褰煎哺锛岃姳鍙朵笉鐩歌?','绾佃韩姝昏?鍚嶇浉瀹堬紝闈掔煶鍙岀?鍒讳袱涓栧悓蹇冦?','寰?敓绨','#9B6BD6',3,1,'2026-11-06 13:58:33','2026-11-06 13:58:33',0),(4,'鏈堣?鎵х瑪锛岀孩绾跨郴瓒','鍗冮噷濮荤紭涓?嚎鐗碉紝浠婂?渚垮湪鎸囧皷鎴愮害銆','濮荤紭绨','#FF6F9C',4,1,'2026-11-06 13:58:33','2026-11-06 13:58:33',0);
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-06 14:42:46
