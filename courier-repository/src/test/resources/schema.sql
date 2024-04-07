------------------------------------------------------------------------
-- 创建表请勿带类似这样的注释（建表会报语法错误）：COMMENT='推送消息描述内容' --
------------------------------------------------------------------------
-- ec_courier2.courier_subscribe definition
CREATE TABLE IF NOT EXISTS `courier_subscribe` (
  `cluster` varchar(64) DEFAULT NULL COMMENT '集群名',
  `service` varchar(64) NOT NULL COMMENT '订阅的服务',
  `mode` varchar(8) NOT NULL DEFAULT 'PUSH' COMMENT '订阅方式',
  `topic` varchar(64) NOT NULL COMMENT 'Topic',
  `group_id` varchar(64) NOT NULL COMMENT '消费组ID',
  `type` varchar(128) NOT NULL COMMENT '消息类型',
  `env_tag` varchar(32) DEFAULT NULL COMMENT '环境标识',
  `subscribed_at` datetime NOT NULL COMMENT '订阅时间',
  UNIQUE KEY `uniq_cluster_service_type` (`cluster`,`service`,`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ec_courier2.courier_subscribe_cluster definition
CREATE TABLE IF NOT EXISTS `courier_subscribe_cluster` (
  `cluster` varchar(32) NOT NULL COMMENT '集群名',
  `env` varchar(32) DEFAULT NULL COMMENT '环境名',
  `url` varchar(255) NOT NULL COMMENT '推送url'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;