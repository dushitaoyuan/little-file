/*
SQLyog Ultimate v12.3.1 (64 bit)
MySQL - 5.7.24 : Database - file
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`file` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `file`;

/*Table structure for table `file` */

DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
  `id` bigint(20) NOT NULL COMMENT '文件id',
  `path` varchar(128) NOT NULL COMMENT '文件路径(相对)',
  `file_type` int(4) DEFAULT NULL COMMENT '文件类型',
  `store_type` tinyint(2) DEFAULT NULL COMMENT '存储类型',
  `file_info` varchar(256) DEFAULT NULL COMMENT '文件信息(json格式)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `path_index` (`path`) COMMENT '路径索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

/*Data for the table `file` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
