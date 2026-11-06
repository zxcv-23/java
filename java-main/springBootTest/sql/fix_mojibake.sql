-- 鬼娘花嫁 · 修复历史遗留的中文乱码（按 data.sql 原始文案，按 id 精确更新）
-- 生成时间：2026-11-06 14:43:57
-- 只更新文本列，不影响 id / 浏览量 / 收藏 / 上下线状态 / 时间等
SET NAMES utf8mb4;

UPDATE `item` SET `title`='月老的红线', `description`='月老以红线系足，千里姻缘一线牵。', `emoji`='🧵', `tag`='月老', `content`='相传月老执掌天下姻缘簿，以红线系于有缘人足踝，虽远隔千山亦终成眷属。', `author`='月老' WHERE `id`=1;
UPDATE `item` SET `title`='三生石上的名字', `description`='三生石上旧精魂，缘定三生不可移。', `emoji`='🪨', `tag`='姻缘簿', `content`='三生石立于忘川畔，刻有情人前世今生来世之名，纵经轮回亦不改其缘。', `author`='月老' WHERE `id`=2;
UPDATE `item` SET `title`='合卺酒', `description`='新人交杯合卺，自此同甘共苦。', `emoji`='🍶', `tag`='红妆', `content`='合卺酒者，破瓠为二瓢，夫妇各执一瓢而饮，象征合二为一、永结同心。', `author`='花娘' WHERE `id`=3;
UPDATE `item` SET `title`='同心结', `description`='以丝绾同心，恩爱两不疑。', `emoji`='🎀', `tag`='姻缘簿', `content`='同心结以锦缕双双编织，赠予新人以为信物，喻心心相印、白首不离。', `author`='花娘' WHERE `id`=4;
UPDATE `item` SET `title`='月老题名簿', `description`='姻缘簿上朱笔一点，良缘就此落定。', `emoji`='📖', `tag`='月老', `content`='月老题名簿录尽凡尘有缘之人，朱笔一圈，便注定三生石上同姓之名。', `author`='月老' WHERE `id`=5;
UPDATE `item` SET `title`='金步摇', `description`='步摇金缕，行步则动摇生姿。', `emoji`='💛', `tag`='红妆', `content`='金步摇为女子首饰，缀以金叶珠翠，行走时轻轻摇曳，添几分娇怯之态。', `author`='红妆阁' WHERE `id`=6;
UPDATE `item` SET `title`='龙凤镯', `description`='一对龙凤，鎏金为盟。', `emoji`='💫', `tag`='红妆', `content`='龙凤镯成双成对，男戴龙女戴凤，乃缔结婚约之重器，喻成双入对。', `author`='红妆阁' WHERE `id`=7;
UPDATE `item` SET `title`='花轿请柬', `description`='大红请柬一封，邀君共赴花轿。', `emoji`='✉️', `tag`='花轿', `content`='花轿请柬书明吉日良辰，遍邀亲友观礼，红纸金笺，喜气盈门。', `author`='花轿信使' WHERE `id`=8;
UPDATE `item` SET `title`='十里红妆单', `description`='十里红妆，铺陈一路繁华。', `emoji`='📜', `tag`='红妆', `content`='十里红妆单详列嫁奁百物，自箱笼镜奁至被褥器皿，极尽人间富贵与祝愿。', `author`='花轿信使' WHERE `id`=9;
UPDATE `item` SET `title`='凤冠霞帔', `description`='凤冠加首，霞帔披身，此生为正室。', `emoji`='👑', `tag`='红妆', `content`='凤冠霞帔为命妇与大婚正礼之服，珠翠累面、云霞满肩，极尽华贵庄重。', `author`='红妆阁' WHERE `id`=10;
UPDATE `item` SET `title`='嫁衣', `description`='一袭嫁衣，缝尽阿娘千针万线。', `emoji`='👰', `tag`='红妆', `content`='嫁衣以大红绸缎裁成，针针线线皆含娘家祝福，穿之于身便是一生托付。', `author`='红妆阁' WHERE `id`=11;
UPDATE `item` SET `title`='胭脂盒', `description`='一点胭脂，染就桃花颜色。', `emoji`='💄', `tag`='红妆', `content`='胭脂盒盛红粉，轻点腮边，便如三月桃花初绽，是新娘妆奁中不可或缺的娇物。', `author`='红妆阁' WHERE `id`=12;
UPDATE `item` SET `title`='往生花', `description`='生于忘川彼岸，花叶永不相见。', `emoji`='🪷', `tag`='往生', `content`='往生花（曼珠沙华）开于黄泉路旁，指引亡魂归往生途，亦喻生死两隔之恋。', `author`='忘川' WHERE `id`=13;
UPDATE `item` SET `title`='忘川渡', `description`='一叶扁舟，渡尽前尘旧事。', `emoji`='🌊', `tag`='往生', `content`='忘川河横亘阴阳，摆渡人载亡魂过河，饮下孟汤，前缘尽忘，唯余此岸花红。', `author`='忘川' WHERE `id`=14;
UPDATE `item` SET `title`='碑上姻缘', `description`='青石为碑，刻一双不灭之名。', `emoji`='🪦', `tag`='往生', `content`='碑上姻缘者，为早逝鸳鸯立双碑于一处，纵身死而名相守，世世可寻。', `author`='往生簿' WHERE `id`=15;
UPDATE `item` SET `title`='鬼娘花嫁总录', `description`='集天下鬼娘花嫁之姻缘于一册。', `emoji`='💞', `tag`='姻缘簿', `content`='鬼娘花嫁总录汇录诸般冥婚典故与良缘，供有缘夫君于此寻觅命定之契。', `author`='月老' WHERE `id`=16;
UPDATE `item` SET `title`='红妆合集', `description`='红妆阁珍藏，件件皆是心头好。', `emoji`='🌹', `tag`='红妆', `content`='红妆合集收尽胭脂钗环、嫁衣镜奁，一览红妆阁之盛，待新人逐件拣选。', `author`='红妆阁' WHERE `id`=17;
UPDATE `item` SET `title`='花轿信使录', `description`='信使奔走阴阳，递送相思。', `emoji`='📜', `tag`='花轿', `content`='花轿信使录记往来阴阳之传书与请柬，纵隔幽明，亦可互通款曲、再缔良约。', `author`='花轿信使' WHERE `id`=18;
UPDATE `category` SET `name`='姻缘簿', `icon`='💞', `description`='天下姻缘尽录于此' WHERE `id`=1;
UPDATE `category` SET `name`='花轿信使', `icon`='📜', `description`='往来阴阳的传书与请柬' WHERE `id`=2;
UPDATE `category` SET `name`='红妆阁', `icon`='🌹', `description`='胭脂钗环与嫁衣镜奁' WHERE `id`=3;
UPDATE `category` SET `name`='往生簿', `icon`='🕯️', `description`='生死两隔之恋' WHERE `id`=4;
UPDATE `category` SET `name`='前世姻缘', `icon`='💞', `description`='三生石上旧精魂' WHERE `id`=5;
UPDATE `category` SET `name`='今生良缘', `icon`='💍', `description`='合卺交杯结同心' WHERE `id`=6;
UPDATE `category` SET `name`='月老题名', `icon`='🌙', `description`='朱笔一点定良缘' WHERE `id`=7;
UPDATE `category` SET `name`='红妆信物', `icon`='🎁', `description`='金缕珠翠为盟' WHERE `id`=8;
UPDATE `category` SET `name`='花轿请柬', `icon`='✉️', `description`='吉日良辰邀观礼' WHERE `id`=9;
UPDATE `category` SET `name`='嫁衣', `icon`='👰', `description`='一袭红裳托终身' WHERE `id`=10;
UPDATE `category` SET `name`='胭脂', `icon`='💄', `description`='桃花颜色点腮边' WHERE `id`=11;
UPDATE `category` SET `name`='往生姻缘', `icon`='🪷', `description`='彼岸花开引归途' WHERE `id`=12;
UPDATE `category` SET `name`='碑铭', `icon`='🪦', `description`='青石双碑守名讳' WHERE `id`=13;
UPDATE `banner` SET `title`='月夜为媒，红妆为信', `subtitle`='以三生石上旧精魂，许你一段不灭姻缘。', `tag`='上元灯会', `accent`='#E84A78' WHERE `id`=1;
UPDATE `banner` SET `title`='十里红妆，铺作星河', `subtitle`='胭脂钗环、凤冠霞帔，皆待有心人逐件拣选。', `tag`='红妆阁', `accent`='#F4D58B' WHERE `id`=2;
UPDATE `banner` SET `title`='忘川彼岸，花叶不相见', `subtitle`='纵身死而名相守，青石双碑刻两世同心。', `tag`='往生簿', `accent`='#9B6BD6' WHERE `id`=3;
UPDATE `banner` SET `title`='月老执笔，红线系足', `subtitle`='千里姻缘一线牵，今夜便在指尖成约。', `tag`='姻缘簿', `accent`='#FF6F9C' WHERE `id`=4;
