-------------
-- ticketsServer方案：ID生成表
-------------
DROP TABLE IF EXISTS `Tickets64` ;
CREATE TABLE `Tickets64` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `stub` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '类型',
  `ts` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stub` (`stub`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='ID生成表';

-------------
-- 数据库批量段方案：ID生成表
-------------
DROP TABLE IF EXISTS `seq_no` ;
CREATE TABLE `seq_no` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `biz_type` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'ID类型',
  `max_id` BIGINT(20) UNSIGNED NOT NULL default '1' COMMENT '当前最大ID',
  `step`   INT(11)     NOT NULL default '1' COMMENT '步长',
  `description` VARCHAR(256) DEFAULT '' COMMENT '备注',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `biz_type` (`biz_type`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库批量段方案：ID生成表';
