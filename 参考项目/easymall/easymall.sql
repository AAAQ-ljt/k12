/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50739 (5.7.39-log)
 Source Host           : localhost:3306
 Source Schema         : easymall

 Target Server Type    : MySQL
 Target Server Version : 50739 (5.7.39-log)
 File Encoding         : 65001

 Date: 14/01/2026 17:00:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent_message
-- ----------------------------
DROP TABLE IF EXISTS `agent_message`;
CREATE TABLE `agent_message`  (
  `message_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `assistant_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'AI消息',
  `user_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户消息',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '0:用户取消 1:回答中 2:完成',
  `biz_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `biz_data` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务数据',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000177 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of agent_message
-- ----------------------------

-- ----------------------------
-- Table structure for order_comment
-- ----------------------------
DROP TABLE IF EXISTS `order_comment`;
CREATE TABLE `order_comment`  (
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `comment_content` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价内容',
  `comment_time` datetime NULL DEFAULT NULL COMMENT '评价时间',
  `comment_images` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价图片',
  `star` int(11) NULL DEFAULT NULL COMMENT '评价星级',
  `comment_biz_reply` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商家回复',
  `recomment_content` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '追评',
  `recomment_time` datetime NULL DEFAULT NULL COMMENT '追评时间',
  `recomment_images` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '追评图片',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `property_info` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性信息',
  `status` tinyint(4) NULL DEFAULT 0 COMMENT '0:正常 1:已删除',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_comment
-- ----------------------------

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`  (
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `order_time` datetime NULL DEFAULT NULL COMMENT '订单创建时间',
  `order_status` tinyint(1) NULL DEFAULT NULL COMMENT '-1已删除 0:待付款 1:已付款,待发货  2:已发货  3:已完成 4:已取消 5:已关闭 6:已退款 7:部分退款',
  `pay_channel` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付通道',
  `pay_scene` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付场景',
  `pay_order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付订单号',
  `channel_order_Id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '通道ID',
  `comment_status` tinyint(4) NULL DEFAULT 0 COMMENT '评价状态 0:未评价  1:已评价  2:已追评',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_pay_order_id`(`pay_order_id`) USING BTREE,
  INDEX `idx_create_time`(`order_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_info
-- ----------------------------

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `order_item_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单明细ID',
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
  `cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面',
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `property_value_id_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性值id组hash',
  `property_info` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性信息',
  `item_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `buy_count` int(11) NULL DEFAULT NULL COMMENT '数量',
  `order_item_status` tinyint(1) NULL DEFAULT NULL COMMENT '状态 1:正常 0:已退款',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `refund_order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '退款订单号',
  PRIMARY KEY (`order_item_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------

-- ----------------------------
-- Table structure for order_logistics_info
-- ----------------------------
DROP TABLE IF EXISTS `order_logistics_info`;
CREATE TABLE `order_logistics_info`  (
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单编号',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `logistics_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物流单号',
  `logistics_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物流公司',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发货人姓名',
  `sender_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发货人电话',
  `sender_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发货地址',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收件人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收件人电话',
  `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收件地址',
  `logistics_status` tinyint(4) NULL DEFAULT 0 COMMENT '物流状态：0待发货 1运输中 2已送达 3订单取消',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_tracking_no`(`logistics_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`(8)) USING BTREE,
  INDEX `idx_logistics_status`(`logistics_status`) USING BTREE,
  INDEX `idx_logistics_company`(`logistics_company`) USING BTREE,
  INDEX `idx_receiver_phone`(`receiver_phone`) USING BTREE,
  INDEX `tracking_no`(`logistics_no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '物流信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_logistics_info
-- ----------------------------

-- ----------------------------
-- Table structure for order_logistics_info_record
-- ----------------------------
DROP TABLE IF EXISTS `order_logistics_info_record`;
CREATE TABLE `order_logistics_info_record`  (
  `record_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单ID',
  `record_time` datetime NULL DEFAULT NULL COMMENT '记录时间',
  `record_address` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '记录地址',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_logistics_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 168 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_logistics_info_record
-- ----------------------------

-- ----------------------------
-- Table structure for product_cart
-- ----------------------------
DROP TABLE IF EXISTS `product_cart`;
CREATE TABLE `product_cart`  (
  `cart_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '购物车ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `property_value_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性值id组',
  `property_value_id_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性值id组hash',
  `buy_count` int(11) NULL DEFAULT NULL COMMENT '数量',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `idx_key`(`product_id`, `property_value_id_hash`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '购物车' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_cart
-- ----------------------------

-- ----------------------------
-- Table structure for product_info
-- ----------------------------
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info`  (
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品名称',
  `product_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '商品描述',
  `cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `category_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类ID',
  `p_category_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类父ID',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '-1:已删除 0:下架  1:上架',
  `min_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '最低价格',
  `max_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '最高价格',
  `total_sale` int(11) NULL DEFAULT 0 COMMENT '销量',
  `commend_type` tinyint(1) NULL DEFAULT 0 COMMENT '0:未推荐 1:已经推荐',
  PRIMARY KEY (`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_info
-- ----------------------------
INSERT INTO `product_info` VALUES ('002502696708211', '电脑机箱商务手提简约便携小机箱mini办公MATX组装黑色高级感', '![](/api/file/getResource?sourceName=202601/pOSm1hSH5DFKPiiZHMbC0IcLPxWOwz.png)\n![](/api/file/getResource?sourceName=202601/3nKlxDcI2HGNxNyjgUTyC41S8HV64x.png)\n![](/api/file/getResource?sourceName=202601/4NnQHwDL0X5va2N0f1d3Rv69wtcg5I.png)\n![](/api/file/getResource?sourceName=202601/c65VIoPLuXjO4UdDbKw9BRv4TQ5vuD.png)\n', '202601/yDyeXqFMo8g708qsqUKrGjadzZDKW1_thumbnail.jpg,202601/oUoZdPZy6XKtfGzteZNurCeXZhgt9F_thumbnail.jpg,202601/xeIcTx7g4bn42uIpZbPMOoRn8Tcljw_thumbnail.jpg,202601/rK4VRindeJvZ1WdDOQs3uGVUgimkk1_thumbnail.jpg,202601/vNhhFGMtNjJnuRrLddIM1oHcxHOVWZ_thumbnail.jpg', '2026-01-10 20:13:42', '20002', '10001', 1, 99.90, 109.90, 1, 1);
INSERT INTO `product_info` VALUES ('006431939640182', '运动鞋', '![](/api/file/getResource?sourceName=202601/p4OShyR5HHgF3IosqsZlkHagOrtzfQ.avif)\n![](/api/file/getResource?sourceName=202601/5evISPVL2NVJXkAlCvAloxwwQVUzeN.avif)\n![](/api/file/getResource?sourceName=202601/Sb0OaCgtC03ZWWQ9rLDJIYlUP3W35D.avif)\n![](/api/file/getResource?sourceName=202601/2gbizw6PUNS9H4Cv6MUrppi4KxgASh.avif)\n', '202601/mnWW5sIIgzad1O88lQbr37ZBcdoAPH_thumbnail.webp,202601/Ftplq4iBJHVaIFEQcBplq3ytcfdQYH_thumbnail.webp,202601/aUPlZu5CbeLkhR57eRnysOrtwDKWbu_thumbnail.webp,202601/SFmzvozAOds35mBZ2R6I3rLHGWemKA_thumbnail.webp,202601/lOsFc1r985WrCylBAy4DSc8fow4MPv_thumbnail.webp', '2026-01-11 21:46:27', '20008', '10002', 1, 150.00, 152.00, 0, 0);
INSERT INTO `product_info` VALUES ('030141672169301', '香酥小黄鱼干香脆黄鱼酥炭烤非油炸小鱼干即食休闲零食整箱鱼干', '![](/api/file/getResource?sourceName=202601/OSbpam3UjsJFSZDMAHOyEG4VsmNGdp.png)\n![](/api/file/getResource?sourceName=202601/9ImOm2eFuqMVzuCxgz0OaS8SMovkP6.png)\n![](/api/file/getResource?sourceName=202601/3wujAr347xasrpppjlixjzhQsPNjT2.png)\n', '202601/84ZurXi8yTadTXSbaiCz5jg9s8krxJ_thumbnail.png,202601/YBimZZxlSr9uqziv4zjJWEbLIPhzJO_thumbnail.png,202601/e4DxHGm7zApJMku5LsJLwCpcrDhTEw_thumbnail.png,202601/2QxMxQOOYANAjzZHivIDuDEslMCknw_thumbnail.png,202601/dXe8ntGXVocLsoc67fhihrCsxcb6O5_thumbnail.png', '2026-01-10 21:58:23', '20021', '10005', 1, 59.90, 59.90, 1, 0);
INSERT INTO `product_info` VALUES ('087985202095055', '正宗风干牛肉干手撕特产内蒙牛肉零食熟食真空包装休闲好吃健康', '![](/api/file/getResource?sourceName=202601/Ekox3rklE5Ge44t6IMtN6fLfmpft8j.png)\n![](/api/file/getResource?sourceName=202601/U4BxEUEfuUTpINz6b6JOM2Rdl2qa9A.png)\n![](/api/file/getResource?sourceName=202601/tCFbQoamCtwn19gZxL5SSKVxLoN8Ff.png)\n![](/api/file/getResource?sourceName=202601/h0uzZQyVud01N0KIQ76QqPXKykAQqF.png)\n', '202601/Wx7saGkbP6mvgEnYO4zHvmCXuzo3Nd_thumbnail.png,202601/0M2WrTHPCkXHjrKIbVnHOtXIh28mjt_thumbnail.png,202601/Esb9lFIlqHbjwCwfoBB1rxoVI9Dkke_thumbnail.png,202601/3Pnmn7gPgABx72TZB2v2HVSGXcbh47_thumbnail.png,202601/wAK308YLhiuVbvckp6OHBudcpNsOKR_thumbnail.png', '2026-01-10 21:55:29', '20022', '10005', 1, 35.90, 99.90, 2, 1);
INSERT INTO `product_info` VALUES ('149434016327682', '叼小刀猫咪项链钛钢不掉色小众简约创意黑猫可爱吊坠学生', '![](/api/file/getResource?sourceName=202601/ALviMTOxaRWUBQEgwBZDRwjnuEwIfZ.png)\n![](/api/file/getResource?sourceName=202601/ohTm5QOvTOJjhQl3yM5jlRtuJwk5ks.png)\n![](/api/file/getResource?sourceName=202601/5uSPkoeNpqqIXnvzE5TtksuqwhQxTh.png)\n![](/api/file/getResource?sourceName=202601/mo3ZIIWHOsueUX6Q0cn0G9ohJXURJO.png)\n', '202601/58YwC2jTFvCpbirshCGh4Clszz0kTb_thumbnail.png,202601/UIyL6N9IriwZ739CHVxZI8ydkVNNzB_thumbnail.png,202601/jubMChstCECl6rlV4IDhkOX67ajV0c_thumbnail.png,202601/0s2ugS4vGC4DqcWvNhzokYhWppP7Pa_thumbnail.png,202601/LiNQnCosYLKGMx4RT6WPAjm7ESU9VF_thumbnail.png', '2026-01-10 21:16:20', '20011', '10002', 1, 29.90, 39.90, 1, 1);
INSERT INTO `product_info` VALUES ('153133309154815', '广椰子700v2老爹鞋男女款2025冬季新款厚底休闲莆田运动鞋子男鞋', '![](/api/file/getResource?sourceName=202601/2wE0hA9MTEjNtcc3VOiTRgFxgKfU7I.png)\n![](/api/file/getResource?sourceName=202601/x19ZKBMw0CxpfZ6vwTExr3Bo2gNeX7.png)\n![](/api/file/getResource?sourceName=202601/6dw9AvcBbyUMWaBI1s16JNO4cKGX12.png)\n![](/api/file/getResource?sourceName=202601/Ob4dgyWXadtadsaBQsvLUPYAgfYWnE.png)\n![](/api/file/getResource?sourceName=202601/Zyb8BzOTePkNzFb67CNFu7rln6Wq4G.png)\n![](/api/file/getResource?sourceName=202601/afnN4G1J2pmw55TzPowx6zUPk0jdcJ.png)\n', '202601/AmIpFUS0u9D1qrWdwvstwT9to728RT_thumbnail.png,202601/4PmkkPH6CDtrCVHsIpf3WOnzpHcAkd_thumbnail.png,202601/uCSGgri8xQPBQ1OqqhQSg8TEc3NVJQ_thumbnail.png,202601/FuHwxowgoHKjwwNN6lwf0r3fc6shbC_thumbnail.png,202601/dhBtP2TyOkz9PvbVHJzoZHSvmlLjWB_thumbnail.png', '2026-01-11 18:22:08', '20010', '10002', 1, 299.00, 299.00, 0, 0);
INSERT INTO `product_info` VALUES ('245524850960683', '鲫鱼竿钓鱼竿手竿溪流碳素野钓竿新手19超轻硬细28调台钓竿鲤鱼杆', '![](/api/file/getResource?sourceName=202601/t1iEkv3Kkj4vXBJWhWCOjudHmuHeTT.png)\n![](/api/file/getResource?sourceName=202601/QK9uLFpENOJDLggRB92MNGzZOpNLfk.png)\n![](/api/file/getResource?sourceName=202601/BBOyZX7yFTaZ6WScgSjb2Fgqs2BQs5.png)\n![](/api/file/getResource?sourceName=202601/AXgkptkGsgG5qd4DY11FsVLTVWjWDe.png)\n![](/api/file/getResource?sourceName=202601/PGa8AfWB60QHNo7MTsgLU2IXV9kLOP.png)\n', '202601/ffDWwEYY2KRIHBBwHKxGTUVW32Oczd_thumbnail.webp,202601/vcwC2gLHDNATw6m3EmSv14NiqSfaY7_thumbnail.webp,202601/3GKfhXOvCGx47JXpd0ii8qu7FSJEWr_thumbnail.webp,202601/ky52Vjf8kmqtjYdFZzTHXtwlwhfKx4_thumbnail.webp,202601/7grZVP5mTZAruQR64r5nfFCJawe7gx_thumbnail.webp', '2026-01-11 14:16:55', '88409', '64617', 1, 18.16, 35.75, 0, 0);
INSERT INTO `product_info` VALUES ('281622101582663', '纯天然精细黄麻床垫椰棕10cm厚乳胶家用定制折叠榻榻米垫纯天然精细黄麻床垫椰棕10cm厚乳胶家用定制折叠榻榻米垫', '![](/api/file/getResource?sourceName=202601/61WZaGMuXZlP7UhvCiwcT7hqyS60eL.png)\n![](/api/file/getResource?sourceName=202601/HTKR1hC13Dy7dG3QBYguFs0ijLx6cV.png)\n![](/api/file/getResource?sourceName=202601/l4ItyIVpJQEN15Xc737BWHimUdC9tM.png)\n![](/api/file/getResource?sourceName=202601/U5LeMj3F60PwD4xFaYWUdxsmnhAMRY.png)\n![](/api/file/getResource?sourceName=202601/Azy2CX0XXEp2rHFOmcDlc6v17ChIcI.png)\n', '202601/EaW5TqXDW5o5Dg0tLtL4MdjMgu84HO_thumbnail.png,202601/LQbwm4DMwPVBXuPbrc4gvuOBvQrNpR_thumbnail.png,202601/DSBb0ZZ0GoVgQK67TaEwhZCPIvwDts_thumbnail.png,202601/vBLr5gaZD3ysyQP16vVSOhTCWmd6Hw_thumbnail.png,202601/5IchYsaIEhHZ2y95Z0luO0D8wrVde7_thumbnail.png', '2026-01-11 18:04:53', '20016', '10004', 1, 529.90, 529.90, 0, 0);
INSERT INTO `product_info` VALUES ('298286497857602', '全新升级晶冻水光唇釉持久口红01号乌木甜茶1.3g全新升级晶冻水光唇釉持久口红01号乌木甜茶1.3g', '![](/api/file/getResource?sourceName=202601/saJTSrqNKuYy0ptwTpj2Kq0nrd5bGP.png)\n![](/api/file/getResource?sourceName=202601/camqKb4YZp3L3KzLMtMNcwt1JxjPVi.png)\n![](/api/file/getResource?sourceName=202601/bODm5UdWo46bMS9SnJIklGtQyMny3l.png)\n', '202601/KuI5GGeCeiYuqv9rZTtn9QWjoH7Q7S_thumbnail.png,202601/pHjG0mfliw7iPUABq3rhu9rbc8Jtmn_thumbnail.png,202601/2tVy0lAP8EbGX4me6f5c6q57yf4l5p_thumbnail.png,202601/Kn9YUT5pwuUmB6iSvPHBr9HrfI4ve6_thumbnail.png,202601/LAiipAPzyFzNCbFvWsKDaD1w68MdJd_thumbnail.png', '2026-01-11 18:11:16', '20012', '10003', 1, 9.90, 9.90, 0, 0);
INSERT INTO `product_info` VALUES ('324369506169687', '官方正品广椰子鞋350暴龙兽男鞋夏季莆田真爆运动鞋女款OG yeezy', '![](/api/file/getResource?sourceName=202601/ZWcVfAOqzUeI3rjPab8rbRXkkSJKGX.png)\n![](/api/file/getResource?sourceName=202601/pLFCwxecdf4lRU7j7niDEXWjJHKGk8.png)\n![](/api/file/getResource?sourceName=202601/39h8CvLlvzHqZjFXML6y3nInb344Mv.png)\n', '202601/SJyEt246UyWN7HVqXFtgGhRpEwFMIL_thumbnail.png,202601/w9tP5yJRPkrJ8OReIDfRYDLRV8qYtk_thumbnail.png,202601/MYPiNQrdeTMIBWJtsR9ycdZwjKy5xk_thumbnail.png,202601/MDjkuiJc7xuX8R5NxPW4jv4dCd419G_thumbnail.png,202601/W5UsSMeEf9PIJrn71ogWUk1378nJBs_thumbnail.png', '2026-01-11 21:09:42', '20010', '10002', 1, 599.00, 599.00, 0, 0);
INSERT INTO `product_info` VALUES ('331567578151750', '冬日快乐漏馅烤蜜薯创意可爱毛绒地瓜挂件卡通蜜薯玩偶生日礼物', '![](/api/file/getResource?sourceName=202601/xraVKm3f3a8qzFsQGGCZIu2QbdsO83.png)\n![](/api/file/getResource?sourceName=202601/Zs4FsEdX7pN1AOXuc2IvhdniHFmEq3.png)\n![](/api/file/getResource?sourceName=202601/XDBHn85LzIdd8AQ9gGSAfkmNWFfiE5.png)\n![](/api/file/getResource?sourceName=202601/8GNf7EGQqetdL7RDEhNxzVb3Ow3DtO.png)\n![](/api/file/getResource?sourceName=202601/qARxZq09Cih7QfUSxyUc5G4bdfwcOC.png)\n', '202601/0RoKN4fk1h6FHqrY9KknIG6KR7NWy8_thumbnail.png,202601/CT89td9Pd9ZRzDTELqezlgQuFpieE5_thumbnail.png,202601/VrJhfijZMjBcoP7sSWaJF9GoZNaVcz_thumbnail.png,202601/9CGC5Q7DcjioJpgiWzidKFxCHG3DBE_thumbnail.png,202601/0C6zUU9UMcWDPosLdiOIxzZi5ItIul_thumbnail.png', '2026-01-11 21:13:30', '88409', '64617', 1, 19.90, 29.90, 0, 0);
INSERT INTO `product_info` VALUES ('378919755916188', '席梦思床垫家用卧室硬椰棕1.5米20cm厚乳胶软垫独立弹簧', '![](/api/file/getResource?sourceName=202601/KLk9YkdbC9djpSV53AEvwUMyKYIgAg.png)\n![](/api/file/getResource?sourceName=202601/bPZfba5lqqYMOE93kPQf29jC8XDbBV.png)\n![](/api/file/getResource?sourceName=202601/PfbKq6irL7Mc8HIpowCkDM89xsdbkH.png)\n![](/api/file/getResource?sourceName=202601/WeM6FvgwfoKIPECWi2bjP5gziGOrcg.png)\n![](/api/file/getResource?sourceName=202601/KIh0lhu59NYMWVQR4ds4dFW9QYKrwL.png)\n', '202601/DP7ki0XqcnDviCofMkIiHlZ6rqfYaE_thumbnail.png,202601/jPT9c0pMqhtLHnT6kRayfoFZq6RbGl_thumbnail.png,202601/S3x7LxccJottSWDo61MStIsNDmSUDN_thumbnail.png,202601/Qz7VhiX7nxJQkiwsq2Pkrq5T5ye6YN_thumbnail.png,202601/p0Z7y8i5tlrYISJHmdEWI3jkhTF2AO_thumbnail.png', '2026-01-11 12:42:52', '20016', '10004', 1, 499.00, 899.00, 0, 0);
INSERT INTO `product_info` VALUES ('422543322296606', '女大童加绒加厚毛衣女秋冬15岁18初高中学生水貂绒翻领针织打底衫', '![](/api/file/getResource?sourceName=202601/EfulgSPGV0kypYNyuXODBmqEkX382I.png)\n![](/api/file/getResource?sourceName=202601/ROgFjsCpV5E5FWdhNjJmLBYtEhLC49.png)\n![](/api/file/getResource?sourceName=202601/kXvCaAarYbEe1SVuVdlTDrtOWEjRB4.png)\n![](/api/file/getResource?sourceName=202601/hYKwzNvYqLlhTSzDZt93aeSQnW5Pb4.png)\n![](/api/file/getResource?sourceName=202601/xl3wv9oQysCHyzFSTpGfolkNnQ70tQ.png)\n', '202601/Es2Me5UbE9voEk0LJ5hv2cgbPXgB0l_thumbnail.jpg,202601/Uj6JdSIBYdZ2XKnp3mZDChMQHY7Kb9_thumbnail.jpg,202601/X5dQaMUMQ2p4b86LViJBdvkrjuZ0a5_thumbnail.jpg,202601/KfF11QY8Fnr1J54e2I3bYH4clRB7XP_thumbnail.jpg,202601/2szKpOb1ZSA7vcVX69pcj6ehALqh0o_thumbnail.jpg', '2026-01-10 20:39:53', '20007', '10002', 1, 39.90, 39.90, 0, 1);
INSERT INTO `product_info` VALUES ('423205878567931', '天然乳胶0胶水床垫卧室护脊弹簧椰棕护脊垫家用抑菌垫子', '![](/api/file/getResource?sourceName=202601/6Zm7zmbhFWSlQqSGy6d9xmms3waXcF.png)\n![](/api/file/getResource?sourceName=202601/1qCdLO0xtjKkj0Zk6ukJvFKTHagJZG.png)\n![](/api/file/getResource?sourceName=202601/sluSbpNYxX60Bdp1hbqifb7HqFlpKp.png)\n![](/api/file/getResource?sourceName=202601/1UQ8fTfmjexiExcKG3Ssd9bjB0Bega.png)\n![](/api/file/getResource?sourceName=202601/FcEEMkcMjETE5UNqS42JgKEYnGc0Po.png)\n', '202601/MAAxdsZ8cwlV9LHNZ3DnTEv4CAGIyI_thumbnail.png,202601/NeAc2D191hZx7weneDu1QvhJd99F2f_thumbnail.png,202601/yM4MVHmR8NEu1ComizOZyRLGHJUura_thumbnail.png,202601/pOmKjfjmgVt43q8JzWn91gvptQCjYI_thumbnail.png,202601/OxDpT0w2Kg3Ei980YBH5VUFj7eOSm3_thumbnail.png', '2026-01-11 12:37:54', '20017', '10004', 1, 1899.00, 2099.00, 0, 0);
INSERT INTO `product_info` VALUES ('438316828084252', '芒果奶糕牛扎芒果干之恋草莓奶糯酪条办公室解馋网红休闲零食小吃', '![](/api/file/getResource?sourceName=202601/3HbMHvk4s58Amos0hT0Kua9lWSh6hN.png)\n![](/api/file/getResource?sourceName=202601/Cih3uo5GvquqCQc9FaoWXsHnJUZVSp.png)\n![](/api/file/getResource?sourceName=202601/oKxGPjW97YLCXaO226JAVn48IxTC0M.png)\n', '202601/i2xGLZRmg1pKVPCJr9jp26J6gscnyV_thumbnail.png,202601/IS7tYU2NlK0LFrG810ZGv99J5xL6Ja_thumbnail.png,202601/nvUsWQENwKmcoboPMZqIbQeenQMNUj_thumbnail.png,202601/FIGbBuGwy3ut39zc85oTcqCsDFxqzi_thumbnail.png,202601/XstrXft7fmQOXVIT73Y7TB9NppfgGw_thumbnail.png', '2026-01-10 21:52:03', '20021', '10005', 1, 39.90, 89.90, 2, 1);
INSERT INTO `product_info` VALUES ('531629223245423', '大码休闲复古长袖针织衫女秋冬半高领内搭打底衫胖MM遮肉显瘦上衣', '![](/api/file/getResource?sourceName=202601/vZfgEVwI1vyFg1SUECjtKWScUgF9vB.png)\n![](/api/file/getResource?sourceName=202601/ovwS3J2bTgPLJmSuFxB5jLveD8ymwZ.png)\n![](/api/file/getResource?sourceName=202601/SFsoFsEQNku9WI0awlJRKlw8LWPftZ.png)\n![](/api/file/getResource?sourceName=202601/QdeW7N7it2x0qOhaCPBpsUs3x9qQk0.png)\n![](/api/file/getResource?sourceName=202601/dgdXQMMlJXOmYIrbF5W0SzSaFzL5uU.png)\n', '202601/oVa6ke9uUOhMIyV6WsBFOWRXLgwfx7_thumbnail.jpg,202601/bbEmj1Zsf0LtqE4WM51eyoVRMUItph_thumbnail.jpg,202601/W2CiDcXN88csOJB28JnWoao2Fge8m8_thumbnail.jpg,202601/laEut2NkJdkXKlaPyzaPB1SmAInqNH_thumbnail.jpg,202601/xeqPLAXH2iSzriiUkTNQbXjgUic2Af_thumbnail.jpg', '2026-01-10 20:34:48', '20007', '10002', 1, 49.90, 49.90, 0, 1);
INSERT INTO `product_info` VALUES ('535739147419699', '108张三国杀壁纸贴纸游戏皮肤图鉴精美壁纸装饰手机壳防水自粘diy', '![](/api/file/getResource?sourceName=202601/AJBbbBSw3i5wQYbVj2ZAShf6zYwzwM.png)\n![](/api/file/getResource?sourceName=202601/gHcRPef8lSHO2Qh2srXrTdgw4b9dBA.png)\n![](/api/file/getResource?sourceName=202601/2xsvdyFCZCgC0HffDo0FKKeSPxjtyl.png)\n![](/api/file/getResource?sourceName=202601/cKHTmdN64HjS38ONczpXfsddy0UJBh.png)\n![](/api/file/getResource?sourceName=202601/w0ktqIFX9415peuEAKFR4csJQN98AI.png)\n![](/api/file/getResource?sourceName=202601/jMlfwfiyeXSAkUbPXuHrNozgWHr3Sp.png)\n', '202601/NPuwyEoOtqfGlCQQ3jtyxianZJ8E7N_thumbnail.png,202601/RAwszl3l1jOBQSvyKbTzSWoa2gj7t5_thumbnail.png,202601/HSpUlVIFE3dbRPgoBbZK54qcBVxltE_thumbnail.png,202601/wX71nAKxNQw3FqCMWVCbTaAfkkvB93_thumbnail.png,202601/yvEMEkwL9IAO6Ft2Zo6sW8q2qPCNi1_thumbnail.png', '2026-01-10 21:23:59', '20016', '10004', 1, 9.90, 10.90, 0, 0);
INSERT INTO `product_info` VALUES ('569096788843148', '芦荟水乳爽肤水男士女保湿补水滋养湿敷水收缩非毛孔', '![](/api/file/getResource?sourceName=202601/FCljy2C4tgSHv1Lm1Tew1WLRIOFfsI.png)\n![](/api/file/getResource?sourceName=202601/k7EosMQkNbDdTDdrcF74oTEnJXEfOF.png)\n![](/api/file/getResource?sourceName=202601/aGGRRMqAEivVDrKq18CrurxuZ0mtna.png)\n![](/api/file/getResource?sourceName=202601/eWhpONCoAMwk6950yBtzU0yB0zGnYx.png)\n![](/api/file/getResource?sourceName=202601/TwHs2ggqCH5Nx7fuz4hvnJcwrTuzAX.png)\n', '202601/XzxsnpPDzJmlgOwlgkuxQFshYGwLhx_thumbnail.png,202601/vfgHcOmPjN4HBCTjAqlbSGMgP06Dxu_thumbnail.png,202601/wcimjbE9UKggibYbDNj50ZwV71OEk9_thumbnail.png,202601/vxfrfBknY69FbwxqWVMFeP5XUC2YRR_thumbnail.png,202601/dSRSAS4mK1vTh0kG2tclmnWkGFo5oH_thumbnail.png', '2026-01-10 21:37:57', '20012', '10003', 1, 59.90, 89.90, 1, 1);
INSERT INTO `product_info` VALUES ('664740861226404', '迷你空气唇釉唇露丝绒雾面哑光口红女1g女生不沾杯', '![](/api/file/getResource?sourceName=202601/3gMe3J9PpAMgFiH6ied2s5wgsiP5IT.png)\n![](/api/file/getResource?sourceName=202601/LCCRtn7OF44Un4xTBQp7rCdKtGjGNf.png)\n![](/api/file/getResource?sourceName=202601/sJP6tw69O4cwIRKDINnx4jonc24v6o.png)\n![](/api/file/getResource?sourceName=202601/cwidFjuHKRQdu23GZ862LPDMN40DQP.png)\n', '202601/akAhLymjUbAnZSDTcPqyWQbkmlxyaI_thumbnail.png,202601/PMFaNfF6XqO7ljcWL07xbdsVDqXFQR_thumbnail.png,202601/lfLVz3KlU3BEzKUR8roduagp4gNgNi_thumbnail.png,202601/uPNraZENFsQyXBYTDF5dGQ9vZAplyb_thumbnail.png,202601/Fk425053famvStg2I0TGS8iEHuibcC_thumbnail.png', '2026-01-11 18:15:56', '20012', '10003', 1, 59.90, 59.90, 0, 0);
INSERT INTO `product_info` VALUES ('685733118738049', '莆田官方正品三杠德训鞋男鞋2025新款真皮复古休闲板鞋运动鞋子女', '![](/api/file/getResource?sourceName=202601/cscxtaHoZHeiZ41odP4vpJtm5Ky4kT.png)\n', '202601/fGCBbFPjjG4dspitY2TIjXBCQe3kAb_thumbnail.png,202601/DuxsRHnEw93wWGYIv5opwVz01OCw6l_thumbnail.png,202601/OrBz4e1L2AL6F1v3snHrKa66UEzetW_thumbnail.png,202601/9YFxym5EhwZWuWfeV4LNgglFjOoepk_thumbnail.png,202601/ffz3LZbGyPRVUCy9VaC3L44K6NY2eY_thumbnail.png', '2026-01-11 21:02:11', '20010', '10002', 1, 399.00, 399.00, 0, 0);
INSERT INTO `product_info` VALUES ('710762418261843', '老罗真迹 白鸟朝凤图 珍藏版', '老罗绝版真迹，喜欢珍藏的朋友可以收藏。 ', '202601/OxhMUyO9qDZrkzqcQkXI1RtZVrrFCI_thumbnail.jpg,202601/rMflXTPfFwuIfrmXJrzGUm3kwinC8S_thumbnail.jpg,202601/dlXpe4g5UymTa5JZqX3uCUeV9UMUMD_thumbnail.jpg,202601/rjmo3ytvaWuttppeG3trSuuAyY6aaV_thumbnail.jpg,202601/tb4UPejRin2XIzXXn8L1ntwnf7lWmh_thumbnail.jpg', '2026-01-11 18:38:41', '88409', '64617', 1, 200.00, 200.00, 0, 1);
INSERT INTO `product_info` VALUES ('746070730569292', '兔子衣服秋冬宠物衣服东北大花袄可爱喜庆保暖衣服龙猫荷兰猪保暖', '![](/api/file/getResource?sourceName=202601/JecSeuGOYMZNFucf8m7leEVCiOxbAv.webp)\n![](/api/file/getResource?sourceName=202601/7MV4ZaoOT6bvPKe3uF3GErNbnhqXUM.webp)\n![](/api/file/getResource?sourceName=202601/at4BPFuxgCsJ1Y2VWEHjN7ZGnnM87A.webp)\n![](/api/file/getResource?sourceName=202601/06GcEmGicS5GfBb9pmrJCgcbxbPpqu.webp)\n![](/api/file/getResource?sourceName=202601/wuEI0WzGgHLMQz3zblMG31HCyn3YS5.webp)\n', '202601/zEBOu3B46jWoByQ7fv24nm8hCp1Oyt_thumbnail.webp,202601/yWbyWSdRT0CZJVn9gS0zqxQTYGcLnq_thumbnail.webp,202601/04BgUF4RK2Y0swdL1l917KBhtyvyzM_thumbnail.webp,202601/IteaKrGXkc2R3QPOU3k2HUSEvLXMz0_thumbnail.webp,202601/0RVJ4nIDGTVGXf3lir9AnASAXeZtMc_thumbnail.webp', '2026-01-10 16:25:08', '88409', '64617', 1, 12.80, 12.80, 2, 0);
INSERT INTO `product_info` VALUES ('754194826041867', '高领加厚毛衣女秋冬2026新款韩版刺绣立领麻花软糯针织衫套头上衣', '![](/api/file/getResource?sourceName=202601/qD0tABPPbgbZbGjjJ0y05zGr440Zpq.png)\n![](/api/file/getResource?sourceName=202601/mTwJUVCBrDAhqJAqd8hoo5Jva4JrCk.png)\n![](/api/file/getResource?sourceName=202601/D9tstXko29Vsq9SttBMurrXiDQNqiR.png)\n![](/api/file/getResource?sourceName=202601/18F3zjbEVZULvAIJFoazIFzsasIHtE.png)\n![](/api/file/getResource?sourceName=202601/ZnNIIKq8DIC6MUB3rDBnJis7X85sKd.png)\n', '202601/B95EKtUISiJlqSI9o7osY0FcGcEuLw_thumbnail.jpg,202601/5liQ5AHGKrxedgGj0VSQcH57OMbTM8_thumbnail.jpg,202601/r16X0XirHBmH4GgIyv1LVjheXVwCZF_thumbnail.jpg,202601/tZVsJTpEuXJrSVFCRa5X8bl0NZbffc_thumbnail.jpg,202601/St4wuLb6WJL5doMJjuz1RIH4cVhme6_thumbnail.jpg', '2026-01-10 20:27:34', '20007', '10002', 1, 79.90, 79.90, 1, 1);
INSERT INTO `product_info` VALUES ('756114288369968', '恋爱星球项链高级感粉钻小爱心脏少女ins风小众轻奢甜美闺蜜学生', '![](/api/file/getResource?sourceName=202601/fw3B1nDWZNiteO5A5hL0sk8kLgNEGK.png)\n![](/api/file/getResource?sourceName=202601/Qwd4yj1Ui3ivCq1a13kUwI76b9WxBb.png)\n![](/api/file/getResource?sourceName=202601/6LeyDsTTZNdHs4J68H4WvCgOUBo9FT.png)\n![](/api/file/getResource?sourceName=202601/D6REHgyb4b1fUnlBZKE7WJjaMQTAtM.png)\n![](/api/file/getResource?sourceName=202601/NxZjKOChZVx8UpXNPgkTez1fl0aUYA.png)\n', '202601/OhuPwd7pIsqEVtsRIGrf98mvrEoBt2_thumbnail.png,202601/nQ6upmbnRGx8RoGaTlZc4kGd5HXMdT_thumbnail.png,202601/5DGzUmKbuTHcps725aF32zYhtr8JfJ_thumbnail.png,202601/Ds4SlgXRoZdmxOm2EsszQWIvcSDd01_thumbnail.png,202601/mbWCkNbXCIYnUVh6gSnav32RBljmd5_thumbnail.png', '2026-01-10 21:12:14', '20011', '10002', 1, 59.90, 59.90, 1, 1);
INSERT INTO `product_info` VALUES ('761711957636857', '大码2025年冬季新款羊羔绒棉服男加厚保暖双面穿潮牌情侣外套潮', '![](/api/file/getResource?sourceName=202601/xblTz3hi8BehTXXIoywaONkVl6S0Vu.png)\n![](/api/file/getResource?sourceName=202601/R3dynfz7Zjdvydm7Op7BZ443j0MbPh.png)\n![](/api/file/getResource?sourceName=202601/bYNhJ1AbWkvdmPRdUzy6mApfvJr7i3.png)\n![](/api/file/getResource?sourceName=202601/lEJrHavfhNA55Bm2LUwxIgBIS7b3rG.png)\n![](/api/file/getResource?sourceName=202601/acF7926i97FMm69GnqPtCs0KrQwG9K.png)\n![](/api/file/getResource?sourceName=202601/VdWzdj6kQ9YhPIYijxuBq04lDecS6U.png)\n![](/api/file/getResource?sourceName=202601/zdjt3JHCUUuK4Do6RNfydeqLQVJkeJ.png)\n', '202601/OvsWdmi3TJjee1f4oICebNNkU8gSzo_thumbnail.png,202601/1ONU7amxRDlF8reIlXdAmcVgXBtdWn_thumbnail.png,202601/FUbBahTmAhrsHW6kAimv3K3KZteYjD_thumbnail.png,202601/vGfxaN10vcULVPI7ibpYuDHZqJi8yP_thumbnail.png,202601/BqXw0tU1UmXSZQbi0cgroNzkyQPoCS_thumbnail.png', '2026-01-10 20:59:53', '20008', '10002', 1, 139.90, 139.90, 1, 0);
INSERT INTO `product_info` VALUES ('763086281772264', '星月糖闺蜜梦幻变色星空项链女 可爱简约星星月亮锁骨链女', '![](/api/file/getResource?sourceName=202601/E9VP2PnlLVJJKTVyAtvZKDMMZXQaS6.png)\n![](/api/file/getResource?sourceName=202601/Ifm5fnM3oCTtc3pqQ3GHIQZuZuNm8u.png)\n![](/api/file/getResource?sourceName=202601/o7ZkMBqMWjL8yD6YFdRtfozHRYefN1.png)\n![](/api/file/getResource?sourceName=202601/S272XFDnJQiQk0dgVA3v27TbsihrIz.png)\n![](/api/file/getResource?sourceName=202601/NaBUf64TBqfslXsZZlJPCcABeUpNl0.png)\n![](/api/file/getResource?sourceName=202601/mlJrXPixw2lv4zv5ORJfheICqNdg3M.png)\n', '202601/naOr2dwJaca3qfUperfNmWdfvWBjjl_thumbnail.png,202601/BVOXVyFntp0EkLGScXNO4JrsZpbz9S_thumbnail.png,202601/GBhkut2AotCbpfRdatkvuM8cNdt7kX_thumbnail.png,202601/ygLSnu2nTpw6jlMWEBBAxkMd6r7NCD_thumbnail.png,202601/mATTdbHRVwMZgX8TwM2H5r3G55mAH8_thumbnail.png', '2026-01-10 21:08:12', '20011', '10002', 1, 19.90, 35.90, 0, 0);
INSERT INTO `product_info` VALUES ('767781456711238', '一体绒保暖加绒加厚圆领刺绣男士毛衣冬季上衣男装内搭针织打底衫', '![](/api/file/getResource?sourceName=202601/NJNlZQLbylzVSFaAUxSnXednWIPCQe.png)\n![](/api/file/getResource?sourceName=202601/myeReOLtWPHujRvNWd65SHfvrtmxzo.png)\n![](/api/file/getResource?sourceName=202601/gOgn9bprH5lKSv0eZ4Q7e0SNNN2Hhj.png)\n![](/api/file/getResource?sourceName=202601/Be9cFKhkFsn8iAmJkRlrQmvZjAlw2M.png)\n![](/api/file/getResource?sourceName=202601/lFKWrttuSeIos2qSbNVdOQ18CsERUA.png)\n![](/api/file/getResource?sourceName=202601/3beHBs0FDdnbLA6gy4Cig9lJZQBZL7.png)\n![](/api/file/getResource?sourceName=202601/N37ukrTtzsW9VHqFAGEK6KNJxlM74i.png)\n![](/api/file/getResource?sourceName=202601/HhJe1pr1TK9dmMzZaCOjyRbXMC0hLi.png)\n', '202601/vIfTSv3E2x0IPcxjO2InvnMFn3A2sg_thumbnail.png,202601/YIbq1uNw9R8ygGsrJEdi8brYXonoxN_thumbnail.png,202601/hQJruqIsIQgUZHNgxbwDKQvctDn1wx_thumbnail.png,202601/5UGem4vkBYCa6P2omN0LePYrB3uDYh_thumbnail.png,202601/xp1p36e2g7UOqt8lQ3aYhFF3djYmUb_thumbnail.png', '2026-01-10 20:51:43', '20008', '10002', 1, 89.90, 89.90, 1, 1);
INSERT INTO `product_info` VALUES ('768845804636453', '水果迷你可爱小样口红套盒哑光学生少女黄皮显白唇膏一盒小巧便携', '![](/api/file/getResource?sourceName=202601/u32sKyk2TkEQ2GdEEcNI6cB8Og3caP.png)\n![](/api/file/getResource?sourceName=202601/Wi6qsGvoRXFMXFmT0aPxMWygOF8oTT.png)\n![](/api/file/getResource?sourceName=202601/CDpyU2s2v39q2EMu2z0wSikNoTNYQG.png)\n![](/api/file/getResource?sourceName=202601/y9wyRP5clRlkTprenBGJUXGoSQobnq.png)\n', '202601/JmI5xTprD9m3tXKhUl8NAAcj9miZ1h_thumbnail.png,202601/nClzMu74nLRSSx8G51Z1VkL47rYMUL_thumbnail.png,202601/lUEuDn4IqAkNccmmYTAkwmjE8izMV8_thumbnail.png,202601/xw7CwaaWCAThbs2HzBL8ah1Irt0jxU_thumbnail.png,202601/9m6PhtnbpcJSBMaJf3HGuc0Op1lqn2_thumbnail.png', '2026-01-10 21:46:26', '20012', '10003', 1, 29.90, 49.90, 1, 1);
INSERT INTO `product_info` VALUES ('843304724668395', '一头哑光一头镜面SHAQINUO双头唇釉镜面水光唇蜜雾面哑光丝绒口红', '![](/api/file/getResource?sourceName=202601/wCh9PVfO7lQJBm08H8mWBK5xzIKimC.png)\n![](/api/file/getResource?sourceName=202601/l4MG9BJNr3FNfZEGyeoWzo6NnyPC9y.png)\n![](/api/file/getResource?sourceName=202601/Bk1L6LZht90LowRMTI4MNCJWY8QqpO.png)\n![](/api/file/getResource?sourceName=202601/KGs4M5UHbVa1CmOzEGqcdN5uh8hY7P.png)\n![](/api/file/getResource?sourceName=202601/wBm32ZONkBD6Ufy8s681ohdkjVnI2C.png)\n', '202601/pUwyKfd2B813LuFGKyp0xAqHKPkWfY_thumbnail.png,202601/aD81Jp6lpqOp3loRPTqS9ehyzt7DaW_thumbnail.png,202601/pjL2kmku4CPjIUNE258Hf26mljBg35_thumbnail.png,202601/BpLcgDRa2eryX5wzdlRf4lqGlUHcEP_thumbnail.png,202601/knNGleTyvNiFblgUYOYMK5YJRoRrtz_thumbnail.png', '2026-01-10 21:42:19', '20012', '10003', 1, 19.90, 19.90, 0, 0);
INSERT INTO `product_info` VALUES ('864824304719236', '假两件格纹翻领棉服外套男冬季新款宽松加绒加厚大码男生夹克', '![](/api/file/getResource?sourceName=202601/F3V11hIPVAnLRtryd7LNiZ2clmfOx0.png)\n![](/api/file/getResource?sourceName=202601/nLBuzMqEC7lzrmVmNIRdRhNqfaB5GN.png)\n![](/api/file/getResource?sourceName=202601/YNs9LB3HzMgeRFXpkjfelun49EL6Bs.png)\n', '202601/pCs23yyJQRaW6Auyj3GYh8UsPM7EuL_thumbnail.jpg,202601/iefsqE9hoZuWkfMddNVcuf6f1mYnik_thumbnail.jpg,202601/7dbQc1kXd0TESCQBH1MwPu8jgBNaci_thumbnail.jpg,202601/mFMN0k8jOfm142zOcX8t7DtLSiHzDt_thumbnail.jpg,202601/1GHEodQXxSC2GGaWkqHeQ5YejviJ63_thumbnail.jpg', '2026-01-10 20:46:26', '20008', '10002', 1, 189.90, 189.90, 1, 0);
INSERT INTO `product_info` VALUES ('917186661226040', '卡皮巴拉软握按动中性笔高颜值胖胖笔熊猫笔减负速干欧包笔 0.5黑色水笔大容量刷题笔st笔头考试专用开学文具', '![](/api/file/getResource?sourceName=202601/IIcM83JFgrYvMNbG4SDsyDjylH3xFs.png)\n![](/api/file/getResource?sourceName=202601/dHjWneG9qknhrfEDbtfvPxWNSm5Wia.png)\n![](/api/file/getResource?sourceName=202601/ZLTEsg8SiCffX03gaDAgcZPjKTcKSp.png)\n![](/api/file/getResource?sourceName=202601/mq5KqE0w7H2vsS1FUGCJceqZHKqsTT.png)\n![](/api/file/getResource?sourceName=202601/SBwVwYTe20X8Wpj4q3arTFg3I5HUR7.png)\n', '202601/SBTrI5RGlJdu4uv7kxygOIK2Kc8iHn_thumbnail.png,202601/a4xh1CACo85Ia6bq2blVDP42xHNJg8_thumbnail.png,202601/YUrbWvpvLnjT91VDRI86PVSnlNiQ9B_thumbnail.png,202601/hGo4ytAgDvAsbXRv85voO0lPTfPvY6_thumbnail.png,202601/ya8PqJD2tGx9ZueV9Dtc2Io60eQj5E_thumbnail.png', '2026-01-11 17:59:21', '88409', '64617', 1, 19.90, 25.90, 0, 0);
INSERT INTO `product_info` VALUES ('996249180877578', '官方正品过验巴黎3xl紫色老爹鞋男女鞋冬季厚底休闲百搭运动鞋潮', '![](/api/file/getResource?sourceName=202601/LeCzVxQVtviQ55R2C6yR8g76g4rKZ3.png)\n![](/api/file/getResource?sourceName=202601/j3hnF6vuttee0h13buXZENxgCksoYe.png)\n![](/api/file/getResource?sourceName=202601/YKG0Rza4DRn3asTp5g6Evh51UjaD1s.png)\n![](/api/file/getResource?sourceName=202601/5Q7PUvAbxfDtBqUwHPEXbcmqJARHEG.png)\n![](/api/file/getResource?sourceName=202601/6HFAlgtkCigcgITVb2c0EiGMPS6ehP.png)\n', '202601/mFcxQ2bfxyJOnQN3X1n7nN9baXwPHy_thumbnail.png,202601/JhxHDzq7g06OIFSuhftxaM6pxkO753_thumbnail.png,202601/XBaQiOnLGUSz16zCAaOLDDFwKMem4d_thumbnail.png,202601/TmEA6AFd4CFmtqTX4Tsrwt56MDgcuU_thumbnail.png,202601/FOhrBiEfb3Kkh1csszbSoZsjvDAvYH_thumbnail.png', '2026-01-11 21:05:42', '20010', '10002', 1, 299.00, 299.00, 0, 0);

-- ----------------------------
-- Table structure for product_property_value
-- ----------------------------
DROP TABLE IF EXISTS `product_property_value`;
CREATE TABLE `product_property_value`  (
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `property_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性ID',
  `property_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性名称',
  `property_sort` int(11) NULL DEFAULT NULL COMMENT '属性排序',
  `cover_type` tinyint(1) NULL DEFAULT NULL COMMENT '0:无需传封面 1:需传封面',
  `property_value_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `property_cover` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性封面',
  `property_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性值',
  `property_remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(11) NULL DEFAULT NULL COMMENT '属性值排序',
  PRIMARY KEY (`product_id`, `property_value_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_property_value
-- ----------------------------
INSERT INTO `product_property_value` VALUES ('002502696708211', '1004', '颜色', 1, 1, '1768047106408', '202601/qAJe6o0cty6XKN3MucqK2ox532PY1h_thumbnail.jpg', '黑色带把手', '', 0);
INSERT INTO `product_property_value` VALUES ('002502696708211', '1004', '颜色', 1, 1, '1768047174846', '202601/5Do6iYMWalssAm1dw4rBCGcmB6omtB_thumbnail.jpg', '白色无把手', '', 1);
INSERT INTO `product_property_value` VALUES ('002502696708211', '1004', '颜色', 1, 1, '1768047179814', '202601/0PNpAVsQqyTizUt8ZyXHKkt9qtVmNZ_thumbnail.jpg', '黑色无把手', '', 2);
INSERT INTO `product_property_value` VALUES ('006431939640182', '1022', '颜色', 1, 1, '1768138969071', '202601/69wLIxigczaNtuJ36kUi7ma3YrggKw_thumbnail.webp', '白色', '', 0);
INSERT INTO `product_property_value` VALUES ('006431939640182', '1023', '尺码', 2, NULL, '1768138969072', '', '38', '', 0);
INSERT INTO `product_property_value` VALUES ('006431939640182', '1022', '颜色', 1, 1, '1768139059869', '202601/xzVfGb4OVEw13JUwHLby9yTDsCqsH5_thumbnail.webp', '黑色', '', 1);
INSERT INTO `product_property_value` VALUES ('006431939640182', '1023', '尺码', 2, NULL, '1768139082786', '', '40', '', 1);
INSERT INTO `product_property_value` VALUES ('006431939640182', '1023', '尺码', 2, NULL, '1768139096198', '', '41', '', 2);
INSERT INTO `product_property_value` VALUES ('030141672169301', '1061', '口味', 1, 1, '1768053360316', '202601/aGYmk1l4Pu0UcMlIrGNSwPZq2o0ZM2_thumbnail.png', '原味', '', 0);
INSERT INTO `product_property_value` VALUES ('030141672169301', '1061', '口味', 1, 1, '1768053484491', '202601/YHOYTLf4FDRvg9BFXyBSn0sXuG0CyD_thumbnail.png', '孜然味', '', 1);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1064', '口味', 1, NULL, '1768053232516', '', '香辣味', '', 0);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1065', '净含量', 2, NULL, '1768053232517', '', '250g', '', 0);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1066', '包装', 3, NULL, '1768053232518', '', '盒装', '', 0);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1064', '口味', 1, NULL, '1768053273259', '', '原味', '', 1);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1065', '净含量', 2, NULL, '1768053282539', '', '500g', '', 1);
INSERT INTO `product_property_value` VALUES ('087985202095055', '1065', '净含量', 2, NULL, '1768053290035', '', '1000g', '', 2);
INSERT INTO `product_property_value` VALUES ('149434016327682', '1031', '颜色', 1, 1, '1768050874181', '202601/vn4dI5IXveeJs87a30AgeYOvSKMNdE_thumbnail.png', '小猫款', '60链长', 0);
INSERT INTO `product_property_value` VALUES ('149434016327682', '1031', '颜色', 1, 1, '1768050942804', '202601/H83aUa6yra0iVfdlamBw7gBkgN6O6b_thumbnail.png', '翻书款', '70链长', 1);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1028', '颜色', 1, 1, '1768126775049', '202601/okWKE9QEBTaUfxjsJV7V5mCSiCSUF0_thumbnail.png', '椰子灰', '', 0);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1029', '尺码', 2, NULL, '1768126775050', '', '38', '', 0);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1028', '颜色', 1, 1, '1768126825416', '202601/QWy83xbz1EujYjKZe2IFi4XFxCknbE_thumbnail.png', '黑色', '', 1);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1028', '颜色', 1, 1, '1768126825960', '202601/Rni66jzkybE8fCq4rZOgrXw6PgD6XZ_thumbnail.png', '加绒 椰子灰', '', 2);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1028', '颜色', 1, 1, '1768126826456', '202601/cEGIQYrnXQebAwGyGVqM66d0eFp0dE_thumbnail.png', '加绒 黑色', '', 3);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1029', '尺码', 2, NULL, '1768126827768', '', '39', '', 1);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1029', '尺码', 2, NULL, '1768126828185', '', '40', '', 2);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1029', '尺码', 2, NULL, '1768126828936', '', '41', '', 3);
INSERT INTO `product_property_value` VALUES ('153133309154815', '1029', '尺码', 2, NULL, '1768126882529', '', '42', '', 4);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112048337', '202601/f9fiqCwdZxAXhSikEywSeR1O2kLL3M_thumbnail.webp', '37调【轻细手感】2.7米无涂装仅重11克', '', 0);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112120218', '202601/RKHR9R9VxQ9MiKHLAf6d4Ug4QXPbaD_thumbnail.webp', '37调【轻细手感】3.6米仅重21克', '', 1);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112131328', '202601/uaSeiWPi9uIP29D472bdE5jYSH7U0w_thumbnail.webp', '37调【轻细手感】3.9米仅重26克', '', 2);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112140992', '202601/gf05QQ7tY7xMlsf0KtCVbCT01DQVwC_thumbnail.webp', '37调【轻细手感】4.5米仅重44克', '', 3);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112152882', '202601/23CHaa3fAC91Lbrtg3HWxL9dCDFfFK_thumbnail.webp', '37调【轻细手感】4.8米仅重47克', '', 4);
INSERT INTO `product_property_value` VALUES ('245524850960683', '44065', '颜色分类', NULL, 1, '1768112163618', '202601/eo2CZhFb1AjFr2FpJyQplExAjb8B52_thumbnail.webp', '37调【轻细手感】5.4米仅重52克', '', 5);
INSERT INTO `product_property_value` VALUES ('281622101582663', '1046', '颜色', 1, 1, '1768125650401', '202601/jBE1cGhU4hrXzAZPRmPSVmloguhkK1_thumbnail.png', '适中A款7CM=防螨有机棉面料+3cm黄麻棕+1cm泰国乳胶', '', 0);
INSERT INTO `product_property_value` VALUES ('281622101582663', '1048', '尺寸', 3, 0, '1768125650402', '', '1000mm*2000mm', '', 0);
INSERT INTO `product_property_value` VALUES ('281622101582663', '1046', '颜色', 1, 1, '1768125702120', '202601/PZcIH3T4dp4mQEA4kxcfxl10YeJ5MS_thumbnail.png', '适中B款7CM=防螨天丝面料+3cm精梳S型黄麻+1cm泰国乳胶', '', 1);
INSERT INTO `product_property_value` VALUES ('298286497857602', '1034', '颜色分类', 1, 1, '1768126212913', '202601/V8IR8T6MrSknir649JaGXIi7pLusIM_thumbnail.png', '#01乌木甜茶 ⭐【HOT】豆蔻粉红棕', '', 0);
INSERT INTO `product_property_value` VALUES ('298286497857602', '1034', '颜色分类', 1, 1, '1768126259416', '202601/PUTcY19SeV0x7qbXACNRZ4gsRTGMwe_thumbnail.png', '#03冷杉红栗 ⭐【NEW】冰透红茶冻', '', 1);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1028', '颜色', 1, 1, '1768136798906', '202601/3LG8sc5GJjKUmbbQmOWvhZREybs9jd_thumbnail.png', '黑色', '', 0);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1029', '尺码', 2, NULL, '1768136798907', '', '38', '', 0);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1028', '颜色', 1, 1, '1768136925890', '202601/KNLK9mdaNYbQSMQpAGxKWo1mFVnm9i_thumbnail.png', '白色', '', 1);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1029', '尺码', 2, NULL, '1768136940273', '', '39', '', 1);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1029', '尺码', 2, NULL, '1768136940905', '', '40', '', 2);
INSERT INTO `product_property_value` VALUES ('324369506169687', '1029', '尺码', 2, NULL, '1768136941281', '', '41', '', 3);
INSERT INTO `product_property_value` VALUES ('331567578151750', '44065', '颜色分类', NULL, 1, '1768137135178', '202601/ze8SbUStnNIs5NjiTOlnkIPLQXC7Ph_thumbnail.png', '烤红薯毛绒挂件【1个】', '', 0);
INSERT INTO `product_property_value` VALUES ('331567578151750', '44065', '颜色分类', NULL, 1, '1768137187569', '202601/eZGhltSI4lQGeIOZEpH2nS0JIhBSey_thumbnail.png', '2只装【烤红薯一只+鲜红薯一只】', '', 1);
INSERT INTO `product_property_value` VALUES ('378919755916188', '1046', '颜色', 1, 1, '1768106374323', '202601/BGzotDpsENEbXn5XJ4kaK2BRMuUrKL_thumbnail.png', '硬核支撑?静音独立弹簧线径加粗 20cm适中', '', 0);
INSERT INTO `product_property_value` VALUES ('378919755916188', '1048', '尺寸', 3, 0, '1768106374324', '', '120x190CM', '', 0);
INSERT INTO `product_property_value` VALUES ('378919755916188', '1046', '颜色', 1, 1, '1768106510018', '202601/T4O7OtGTA32C4Y4LGJM9t4szyWAxLY_thumbnail.png', '实惠首选白碳钢加固弹簧+3E棕 均衡承托 22cm偏硬', '', 1);
INSERT INTO `product_property_value` VALUES ('378919755916188', '1046', '颜色', 1, 1, '1768106511697', '202601/0adGwg1hQSkuaUjcbzL0PPOsflw8P1_thumbnail.png', '尊享环保抑菌竹炭分区静音布袋弹簧+3E棕 22cm厚偏硬', '', 2);
INSERT INTO `product_property_value` VALUES ('378919755916188', '1048', '尺寸', 3, 0, '1768106553530', '', '120x200CM', '', 1);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1019', '颜色', 1, 1, '1768048722327', '202601/jO123JRYUau2unIfG8qBZxCZqf4AW4_thumbnail.jpg', '蓝色', '', 0);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1020', '尺码', 2, NULL, '1768048722328', '', 'S', '', 0);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1019', '颜色', 1, 1, '1768048730485', '202601/eJMAlt87MNyt3HnUDfVsUMafyteS3J_thumbnail.jpg', '绿色', '', 1);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1019', '颜色', 1, 1, '1768048730829', '202601/vQUUGtI45BhStNv2DlzHsTAJyoFfHZ_thumbnail.jpg', '粉色', '', 2);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1019', '颜色', 1, 1, '1768048731301', '202601/aN9Xn92lFdL8oLVbiEbkUm1QGRcuHO_thumbnail.jpg', '白色', '', 3);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1020', '尺码', 2, NULL, '1768048758373', '', 'M', '', 1);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1020', '尺码', 2, NULL, '1768048758813', '', 'L', '', 2);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1020', '尺码', 2, NULL, '1768048759629', '', 'XL', '', 3);
INSERT INTO `product_property_value` VALUES ('422543322296606', '1020', '尺码', 2, NULL, '1768048766773', '', '2XL', '', 4);
INSERT INTO `product_property_value` VALUES ('423205878567931', '1049', '颜色', 1, 1, '1768106050739', '202601/XfgikiiowURmM7Wac5oObDuNUrBKfn_thumbnail.png', '(全垫零胶水)椰椰Pro·基础款(22cm厚)J56P', '', 0);
INSERT INTO `product_property_value` VALUES ('423205878567931', '1050', '尺寸', 2, NULL, '1768106050740', '', '1200mm*1900mm', '', 0);
INSERT INTO `product_property_value` VALUES ('423205878567931', '1049', '颜色', 1, 1, '1768106208682', '202601/jqLX8G0EVClWLXIKoiAec5iaJiZvqS_thumbnail.png', '(全垫零胶水)椰椰Pro·升级款(25cm厚)J56P', '', 1);
INSERT INTO `product_property_value` VALUES ('423205878567931', '1050', '尺寸', 2, NULL, '1768106233346', '', '1350mm*1900mm', '', 1);
INSERT INTO `product_property_value` VALUES ('438316828084252', '1061', '口味', 1, 1, '1768052963764', '202601/T61o37DdrUL3JbocBDDNKMa7eniOdi_thumbnail.png', '整箱10包', '买5送5', 0);
INSERT INTO `product_property_value` VALUES ('438316828084252', '1061', '口味', 1, 1, '1768053085531', '202601/jq28F1PGLFvgzY7ZxUdmfDtmJIvjUE_thumbnail.png', '整箱30包', '买15送15', 1);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1019', '颜色', 1, 1, '1768048393199', '202601/4W6eaoTE6ywm0W6RADN4ilJ9b0wsKj_thumbnail.jpg', '燕麦色', '', 0);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1020', '尺码', 2, NULL, '1768048393200', '', 'M', '', 0);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1019', '颜色', 1, 1, '1768048397045', '202601/HnZVC2SRjiMK8Q3DQVZLRZrLuHfXU0_thumbnail.jpg', '黑色', '', 1);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1019', '颜色', 1, 1, '1768048397525', '202601/iT5iIxSS3jl1oZiPmJiQfsdskBHWhs_thumbnail.jpg', '白色', '', 2);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1020', '尺码', 2, NULL, '1768048435085', '', 'L', '', 1);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1020', '尺码', 2, NULL, '1768048435461', '', 'XL', '', 2);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1020', '尺码', 2, NULL, '1768048435869', '', '2XL', '', 3);
INSERT INTO `product_property_value` VALUES ('531629223245423', '1020', '尺码', 2, NULL, '1768048436157', '', '3XL', '', 4);
INSERT INTO `product_property_value` VALUES ('535739147419699', '1046', '颜色', 1, NULL, '1768051305653', '', '黑色', '', 0);
INSERT INTO `product_property_value` VALUES ('535739147419699', '1048', '材质', 3, NULL, '1768051305654', '', '防水pvc', '', 0);
INSERT INTO `product_property_value` VALUES ('535739147419699', '1046', '颜色', 1, NULL, '1768051377724', '', '彩色', '', 1);
INSERT INTO `product_property_value` VALUES ('535739147419699', '1048', '材质', 3, NULL, '1768051397428', '', '普通pvc', '', 1);
INSERT INTO `product_property_value` VALUES ('569096788843148', '1034', '色号', 1, 1, '1768052200558', '202601/PtR1qJkyFFbIPKQasDiBezBKy8wNUc_thumbnail.png', '芦荟胶', '', 0);
INSERT INTO `product_property_value` VALUES ('569096788843148', '1035', '容量', 2, NULL, '1768052200559', '', '500ml', '', 0);
INSERT INTO `product_property_value` VALUES ('569096788843148', '1035', '容量', 2, NULL, '1768052244044', '', '1000ml', '', 1);
INSERT INTO `product_property_value` VALUES ('664740861226404', '1034', '颜色分类', 1, 1, '1768126444657', '202601/555DX0Iy22dLM58IEOcDnFAPNkHjQJ_thumbnail.png', ' 0308 雨后木棉', '', 0);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1028', '颜色', 1, 1, '1768136388210', '202601/TUuNX69WemaROLTyurOfTNDJyZNF8p_thumbnail.png', '蓝色绒面', '', 0);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1029', '尺码', 2, NULL, '1768136388211', '', '39', '', 0);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1028', '颜色', 1, 1, '1768136464497', '202601/q4bbYSFVsMssSzKdb51MX0yjZsFaC7_thumbnail.png', '白色绒面', '', 1);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1029', '尺码', 2, NULL, '1768136476225', '', '40', '', 1);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1029', '尺码', 2, NULL, '1768136476616', '', '41', '', 2);
INSERT INTO `product_property_value` VALUES ('685733118738049', '1029', '尺码', 2, NULL, '1768136485609', '', '42', '', 3);
INSERT INTO `product_property_value` VALUES ('710762418261843', '44065', '颜色分类', NULL, 1, '1768127788649', '202601/GuYJrcCNtdP40FBrRKbruGybn1xj3Q_thumbnail.jpg', '真迹01', '', 0);
INSERT INTO `product_property_value` VALUES ('710762418261843', '44065', '颜色分类', NULL, 1, '1768127883444', '202601/1LYQI6UCw3aJf81D7yxztgbjaUtWeq_thumbnail.jpg', '真迹02', '', 1);
INSERT INTO `product_property_value` VALUES ('710762418261843', '44065', '颜色分类', NULL, 1, '1768127887012', '202601/G2UH40KlFAGIeWCCyR6h1XFZE7yCOy_thumbnail.jpg', '真迹03', '', 2);
INSERT INTO `product_property_value` VALUES ('710762418261843', '44065', '颜色分类', NULL, 1, '1768127890222', '202601/kOpYHAyTY26VTY1L3aZCxJbXfHfqPs_thumbnail.jpg', '真迹04', '', 3);
INSERT INTO `product_property_value` VALUES ('710762418261843', '44065', '颜色分类', NULL, 1, '1768127896878', '202601/AZJXHV2aOeSjJjsgnJJh51YPoPhIKP_thumbnail.jpg', '真迹05', '', 4);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033276450', '202601/XQIpnOFvOShw7IU8np7NKfW52PoMRq_thumbnail.webp', '东北大花XS 建议1-3斤 具体参考详情页尺码表', '', 0);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033372171', '202601/zEBqRv3FF84jhqlmcUhYLhalOzFCab_thumbnail.webp', '东北大花S 建议3-5斤 具体参考详情页尺码表', '', 1);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033393748', '202601/gp5dxDmSAHCgvlqdkH4g9Qirn2Sgkv_thumbnail.webp', '粉色小兔', 'XXS 建议0.5-1.5斤 胸围22cm背长14cm', 2);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033412137', '202601/jLpz4N42sHtaGbqm7YXCao3OWWlv3s_thumbnail.webp', '粉色小兔', 'XS 建议1.5-3斤 胸围26cm背长17cm', 3);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033437679', '202601/0dlcYhxrHQttoVSNjbp2A7x6J0IJmE_thumbnail.webp', '粉色蜜蜂', 'XS 建议1.5-3斤 胸围26cm背长17cm', 4);
INSERT INTO `product_property_value` VALUES ('746070730569292', '44065', '颜色分类', NULL, 1, '1768033471342', '202601/GSdkTJO3uVxpaJwOtut6GPUFV0VqGT_thumbnail.webp', '黄色蜜蜂', 'XXS 建议0.5-1.5斤 胸围22cm背长14cm', 5);
INSERT INTO `product_property_value` VALUES ('754194826041867', '1019', '颜色', 1, 1, '1768047696583', '202601/q02wrSWuxRjjUtHxTMvY9RM3AGz0nr_thumbnail.jpg', '白色', '', 0);
INSERT INTO `product_property_value` VALUES ('754194826041867', '1020', '尺码', 2, NULL, '1768047696584', '', 'S', '', 0);
INSERT INTO `product_property_value` VALUES ('754194826041867', '1019', '颜色', 1, 1, '1768048005702', '202601/eeUm9chWKUMhup4K0wcFkUs23KsrwK_thumbnail.jpg', '米色', '', 1);
INSERT INTO `product_property_value` VALUES ('754194826041867', '1020', '尺码', 2, NULL, '1768048035229', '', 'M', '', 1);
INSERT INTO `product_property_value` VALUES ('754194826041867', '1020', '尺码', 2, NULL, '1768048038181', '', 'L', '', 2);
INSERT INTO `product_property_value` VALUES ('756114288369968', '1031', '颜色', 1, 1, '1768050669517', '202601/Reqkfr86ypNUF4Kpo8ylhM8fatNDgx_thumbnail.png', '粉钻款', '', 0);
INSERT INTO `product_property_value` VALUES ('756114288369968', '1031', '颜色', 1, 1, '1768050729196', '202601/Eg3pIK4dEhGRN1y6A7nkO4uzD3MZ15_thumbnail.png', '白砖款', '', 1);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1022', '颜色', 1, 1, '1768049856622', '202601/FuPMR7U5lu05CeBrHJ0OaHD70zFdPb_thumbnail.png', '绿色', '双面穿', 0);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1023', '尺码', 2, NULL, '1768049856623', '', 'S', '', 0);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1022', '颜色', 1, 1, '1768049881653', '202601/Zh3drRL6aUwxSwTJxSiXhcmjNe62KA_thumbnail.png', '白色', '', 1);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1022', '颜色', 1, 1, '1768049882109', '202601/JnmLauP8vjgyrgi4ivys6v1luDEntM_thumbnail.png', '紫色', '', 2);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1022', '颜色', 1, 1, '1768049889269', '202601/kkGVWqQoS10tzj5Vxjgiqr87nNTbFO_thumbnail.png', '蓝色', '', 3);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1022', '颜色', 1, 1, '1768049889573', '202601/ZIpfyOPfEsHn3W3E0HcMK5zzDwiw1y_thumbnail.png', '粉色', '', 4);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1023', '尺码', 2, NULL, '1768049931517', '', 'M', '', 1);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1023', '尺码', 2, NULL, '1768049931861', '', 'L', '', 2);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1023', '尺码', 2, NULL, '1768049932292', '', 'XL', '', 3);
INSERT INTO `product_property_value` VALUES ('761711957636857', '1023', '尺码', 2, NULL, '1768049939892', '', '2XL', '', 4);
INSERT INTO `product_property_value` VALUES ('763086281772264', '1031', '颜色', 1, 1, '1768050383077', '202601/7yB3uvvq0SutEBoFA2er2zHCtIuEua_thumbnail.png', '星星糖项链', '', 0);
INSERT INTO `product_property_value` VALUES ('763086281772264', '1031', '颜色', 1, 1, '1768050429180', '202601/3BhSu5XC8i9HxANRPzm5Los6AqpJp1_thumbnail.png', '月亮糖项链', '', 1);
INSERT INTO `product_property_value` VALUES ('763086281772264', '1031', '颜色', 1, 1, '1768050433492', '202601/rraXYFPem4YdEiRQgRHmCLKPxfpuy7_thumbnail.png', '【星星+月亮】闺蜜两条装', '', 2);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1022', '颜色', 1, 1, '1768049438134', '202601/yimDg1N3MlAOqBZYF3VddDoV1nhAv2_thumbnail.png', '浅灰色', '', 0);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1023', '尺码', 2, NULL, '1768049438135', '', 'L', '', 0);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1022', '颜色', 1, 1, '1768049441525', '202601/MtWlYn2Cl0wA1IeUeqJWFkTmrFCiSZ_thumbnail.png', '浅米色', '', 1);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1022', '颜色', 1, 1, '1768049442356', '202601/PEEwXjnnRyHvfoMj2F1qGLNeoI86VY_thumbnail.png', '棕色', '', 2);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1022', '颜色', 1, 1, '1768049442829', '202601/vOFkqXgfhyUnRgGNX9t1pvCl0DhCiz_thumbnail.png', '藏青色', '', 3);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1022', '颜色', 1, 1, '1768049443357', '202601/nzmNsxetvtbqQwMgwRY1oxCUJJC1sF_thumbnail.png', '黑色', '', 4);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1023', '尺码', 2, NULL, '1768049472973', '', 'XL', '', 1);
INSERT INTO `product_property_value` VALUES ('767781456711238', '1023', '尺码', 2, NULL, '1768049473437', '', '2XL', '', 2);
INSERT INTO `product_property_value` VALUES ('768845804636453', '1034', '色号', 1, 1, '1768052687260', '202601/7iEiRkNEGs1ObFwRo2ZXORVv2l1Ai2_thumbnail.png', '莓有烦恼', '', 0);
INSERT INTO `product_property_value` VALUES ('768845804636453', '1035', '容量', 2, NULL, '1768052687261', '', '5支装', '', 0);
INSERT INTO `product_property_value` VALUES ('768845804636453', '1034', '色号', 1, 1, '1768052716419', '202601/xKgKw6dddKOvEp5dHX6OVrKm0VIfDa_thumbnail.png', '富得牛油', '', 1);
INSERT INTO `product_property_value` VALUES ('768845804636453', '1035', '容量', 2, NULL, '1768052752308', '', '10支装', '', 1);
INSERT INTO `product_property_value` VALUES ('843304724668395', '1034', '色号', 1, 1, '1768052450716', '202601/2t29eNMx9D8QNBUmlISivOP2hT4YUG_thumbnail.png', '不累的工', '', 0);
INSERT INTO `product_property_value` VALUES ('843304724668395', '1035', '容量', 2, NULL, '1768052450717', '', '10克', '', 0);
INSERT INTO `product_property_value` VALUES ('843304724668395', '1034', '色号', 1, 1, '1768052488467', '202601/VAx20TgrYMsDZus2zE4EXJWAozfUkb_thumbnail.png', '舒服的假', '', 1);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1022', '颜色', 1, 1, '1768049124430', '202601/mX4mH55w82TrfH6N4oKGzHJZDxokS2_thumbnail.jpg', '棕色', '升级加绒款', 0);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1023', '尺码', 2, NULL, '1768049124431', '', 'M', '', 0);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1022', '颜色', 1, 1, '1768049128413', '202601/phDNO58hqftcZJOVaRTwVzfShk1cwo_thumbnail.jpg', '黑色', '加绒款', 1);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1023', '尺码', 2, NULL, '1768049160885', '', 'L', '', 1);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1023', '尺码', 2, NULL, '1768049161189', '', 'XL', '', 2);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1023', '尺码', 2, NULL, '1768049161605', '', '2XL', '', 3);
INSERT INTO `product_property_value` VALUES ('864824304719236', '1023', '尺码', 2, NULL, '1768049168829', '', '3XL', '', 4);
INSERT INTO `product_property_value` VALUES ('917186661226040', '44065', '颜色分类', NULL, 1, '1768125478450', '202601/0vNcqiRrMPNzWBFPZjxIV2DogbkO0r_thumbnail.png', '【五款大合集】20支装 (超高价值)', '', 0);
INSERT INTO `product_property_value` VALUES ('917186661226040', '44065', '颜色分类', NULL, 1, '1768125501664', '202601/3R5zRwRJBpqerghogeBRI933ahVeIg_thumbnail.png', ' 豚豚店员-4支装 (升级新款)', '', 1);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1028', '颜色', 1, 1, '1768136660947', '202601/U86YUiNk36qz6uWx4CEJQbg1PQOX1f_thumbnail.png', '3XL-暗紫做旧【官方品质】 顺丰+防伪包装', '', 0);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1029', '尺码', 2, NULL, '1768136660948', '', '36', '', 0);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1028', '颜色', 1, 1, '1768136672345', '202601/nZl9E43vhs2Tb100YqM6q7eo4Shxkk_thumbnail.png', '3XL-白红【官方品质】 顺丰+防伪包装', '', 1);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1029', '尺码', 2, NULL, '1768136704401', '', '37', '', 1);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1029', '尺码', 2, NULL, '1768136704993', '', '38', '', 2);
INSERT INTO `product_property_value` VALUES ('996249180877578', '1029', '尺码', 2, NULL, '1768136714065', '', '39', '', 3);

-- ----------------------------
-- Table structure for product_sku
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku`  (
  `product_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品ID',
  `property_value_id_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性值id组hash',
  `property_value_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性值id组',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `stock` int(11) NULL DEFAULT NULL COMMENT '库存',
  `sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`product_id`, `property_value_id_hash`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_sku
-- ----------------------------
INSERT INTO `product_sku` VALUES ('002502696708211', '24f60d8a2b3307c7174f676d36360197', '1768047106408', 109.90, 100, 0);
INSERT INTO `product_sku` VALUES ('002502696708211', 'd734d61009cf36089849df1b504b368c', '1768047179814', 99.90, 100, 2);
INSERT INTO `product_sku` VALUES ('002502696708211', 'd7af348513aea132c4365558081e0c4a', '1768047174846', 99.90, 99, 1);
INSERT INTO `product_sku` VALUES ('006431939640182', '134e6c40f807eae26e19f34a7b622686', '1768138969071-1768139082786', 150.00, 100, 1);
INSERT INTO `product_sku` VALUES ('006431939640182', '3d37bd52c74ad1e7d028a7e8d87c6d9d', '1768138969071-1768139096198', 150.00, 100, 2);
INSERT INTO `product_sku` VALUES ('006431939640182', '5b483ba676d3b9831dff03cc01b611f3', '1768139059869-1768139082786', 151.00, 100, 4);
INSERT INTO `product_sku` VALUES ('006431939640182', '69b1b07479ffd89ef62a024ab66c2aac', '1768139059869-1768139096198', 152.00, 100, 5);
INSERT INTO `product_sku` VALUES ('006431939640182', '892e2660fd87f6fa64f1cb101dc261f8', '1768139059869-1768138969072', 150.00, 100, 3);
INSERT INTO `product_sku` VALUES ('006431939640182', 'e03f9b8dbf4d74b9045adc90125eeabb', '1768138969071-1768138969072', 150.00, 98, 0);
INSERT INTO `product_sku` VALUES ('030141672169301', 'ac1a0362df71363b9b41a36accb85c32', '1768053484491', 59.90, 200, 1);
INSERT INTO `product_sku` VALUES ('030141672169301', 'd224d14fbc3922f1700ce01ccc15a5c8', '1768053360316', 59.90, 198, 0);
INSERT INTO `product_sku` VALUES ('087985202095055', '16199d75b050c2149a932e98519364a6', '1768053232516-1768053232517-1768053232518', 35.90, 197, 0);
INSERT INTO `product_sku` VALUES ('087985202095055', '494b2899b8a0fe1ffc8295a0a0281a96', '1768053273259-1768053232517-1768053232518', 35.90, 200, 3);
INSERT INTO `product_sku` VALUES ('087985202095055', '644816641fa5ca4d87ef415a6e69c8ef', '1768053232516-1768053282539-1768053232518', 69.90, 200, 1);
INSERT INTO `product_sku` VALUES ('087985202095055', 'c6a9967e02af679e84b1f67dc8066d68', '1768053273259-1768053282539-1768053232518', 69.90, 200, 4);
INSERT INTO `product_sku` VALUES ('087985202095055', 'eae7f9c51600058bfc3e324b342f3eef', '1768053232516-1768053290035-1768053232518', 99.90, 200, 2);
INSERT INTO `product_sku` VALUES ('087985202095055', 'ee3d1c263258ae96c8b75f964672fb07', '1768053273259-1768053290035-1768053232518', 99.90, 200, 5);
INSERT INTO `product_sku` VALUES ('149434016327682', 'c3f61f412f3aaac63b02022a92da46cd', '1768050874181', 29.90, 199, 0);
INSERT INTO `product_sku` VALUES ('149434016327682', 'cdc1ad79dcb5ae4507ff8c9d1cebe033', '1768050942804', 39.90, 199, 1);
INSERT INTO `product_sku` VALUES ('153133309154815', '0b577f0005beb0c973284ee350a8a7c8', '1768126825416-1768126828185', 299.00, 100, 7);
INSERT INTO `product_sku` VALUES ('153133309154815', '132df5f207c2c87e6e0fa38175015b89', '1768126825960-1768126827768', 299.00, 100, 11);
INSERT INTO `product_sku` VALUES ('153133309154815', '13c909f5c667233b3a3f3cfebd8f32e3', '1768126825416-1768126828936', 299.00, 100, 8);
INSERT INTO `product_sku` VALUES ('153133309154815', '29f3045cce59057bdf2c4d7b8172af33', '1768126825416-1768126882529', 299.00, 100, 9);
INSERT INTO `product_sku` VALUES ('153133309154815', '342c7715acf857a525071a4d4c9886ef', '1768126825960-1768126828185', 299.00, 100, 12);
INSERT INTO `product_sku` VALUES ('153133309154815', '44e1319f5c710fbf44a7f9a8fdaf7617', '1768126775049-1768126828936', 299.00, 100, 3);
INSERT INTO `product_sku` VALUES ('153133309154815', '5ba0346a143966f2b67db439e03878ad', '1768126826456-1768126775050', 299.00, 100, 15);
INSERT INTO `product_sku` VALUES ('153133309154815', '6bfc932bf2a2882456a735d54ea11ca2', '1768126825960-1768126775050', 299.00, 100, 10);
INSERT INTO `product_sku` VALUES ('153133309154815', '8179e8746e4bac1301e6daac6989c464', '1768126826456-1768126827768', 299.00, 100, 16);
INSERT INTO `product_sku` VALUES ('153133309154815', '8df60cdfc56bc96658bdf0c9d2d93824', '1768126826456-1768126828185', 299.00, 100, 17);
INSERT INTO `product_sku` VALUES ('153133309154815', '8fe4b4072aab9bcc76105ec923862527', '1768126826456-1768126882529', 299.00, 100, 19);
INSERT INTO `product_sku` VALUES ('153133309154815', '98e0bb1b5f627ebbf7b64aeee4b6d678', '1768126825960-1768126882529', 299.00, 100, 14);
INSERT INTO `product_sku` VALUES ('153133309154815', '9d295634bae9aa3fb859ad74dbb9e77a', '1768126775049-1768126828185', 299.00, 100, 2);
INSERT INTO `product_sku` VALUES ('153133309154815', '9e15e57828b95df71656cd21d997f737', '1768126826456-1768126828936', 299.00, 100, 18);
INSERT INTO `product_sku` VALUES ('153133309154815', 'add1154f0f327459b962116d754f72d4', '1768126825416-1768126827768', 299.00, 100, 6);
INSERT INTO `product_sku` VALUES ('153133309154815', 'bcb38c9a7dddce05f554f6382285919b', '1768126825416-1768126775050', 299.00, 100, 5);
INSERT INTO `product_sku` VALUES ('153133309154815', 'bd34a2ea08b8ae40cc3a9baf2fb1c5ca', '1768126775049-1768126882529', 299.00, 100, 4);
INSERT INTO `product_sku` VALUES ('153133309154815', 'be80dc99dbaca48bf5db4274a5644547', '1768126825960-1768126828936', 299.00, 100, 13);
INSERT INTO `product_sku` VALUES ('153133309154815', 'bf7c4e1cfd6f21239a746354b41e6679', '1768126775049-1768126775050', 299.00, 100, 0);
INSERT INTO `product_sku` VALUES ('153133309154815', 'eac15e3e4d736243deb542b6d9d4b147', '1768126775049-1768126827768', 299.00, 100, 1);
INSERT INTO `product_sku` VALUES ('245524850960683', '5119c37ecf77f9b64d23f457c5d1ce88', '1768112163618', 35.75, 270, 5);
INSERT INTO `product_sku` VALUES ('245524850960683', '63ec03192da0bdf7b9c6cc2309569700', '1768112131328', 19.50, 239, 2);
INSERT INTO `product_sku` VALUES ('245524850960683', '64665c3b61565680f274156a644aa961', '1768112140992', 22.75, 400, 3);
INSERT INTO `product_sku` VALUES ('245524850960683', 'd1f13c12710f53a8dd87a45aac8227b8', '1768112048337', 32.50, 340, 0);
INSERT INTO `product_sku` VALUES ('245524850960683', 'efac1a85408066900deeaa00355af989', '1768112120218', 18.16, 187, 1);
INSERT INTO `product_sku` VALUES ('245524850960683', 'f29e01c01205ff2eb6be1ccdf3b50f70', '1768112152882', 29.25, 14, 4);
INSERT INTO `product_sku` VALUES ('281622101582663', '316c3aa224e18cd875ad37f5697edc5e', '1768125702120-1768125650402', 529.90, 100, 1);
INSERT INTO `product_sku` VALUES ('281622101582663', '33263e9e39f59cb86e47d65eb4e30666', '1768125650401-1768125650402', 529.90, 100, 0);
INSERT INTO `product_sku` VALUES ('298286497857602', '71a51ff8020ea17f1b7bd9c7cecf2afb', '1768126259416', 9.90, 200, 1);
INSERT INTO `product_sku` VALUES ('298286497857602', 'ee6f3d95fc7a1ac431e4e7c9b0a9a8ac', '1768126212913', 9.90, 200, 0);
INSERT INTO `product_sku` VALUES ('324369506169687', '0ef7cc1b343222ebf7c8701f54f02430', '1768136925890-1768136940905', 599.00, 300, 6);
INSERT INTO `product_sku` VALUES ('324369506169687', '113515d71985b2de37f5904857462a6b', '1768136798906-1768136798907', 599.00, 300, 0);
INSERT INTO `product_sku` VALUES ('324369506169687', '185b5a87bdd68f1ae258d3dd71cf5edd', '1768136798906-1768136940905', 599.00, 300, 2);
INSERT INTO `product_sku` VALUES ('324369506169687', '27363f7fe4a618b2610b02ff1294166c', '1768136798906-1768136941281', 599.00, 300, 3);
INSERT INTO `product_sku` VALUES ('324369506169687', '2a702f114d1609becaa754e6ef584426', '1768136925890-1768136941281', 599.00, 300, 7);
INSERT INTO `product_sku` VALUES ('324369506169687', '53fd59a9e16e114672d191dddbd4c411', '1768136798906-1768136940273', 599.00, 300, 1);
INSERT INTO `product_sku` VALUES ('324369506169687', '5f866698b3963fdae74771cf71e7db49', '1768136925890-1768136940273', 599.00, 300, 5);
INSERT INTO `product_sku` VALUES ('324369506169687', 'e74f6e851b71d206b1bf47b2a16f9bf4', '1768136925890-1768136798907', 599.00, 300, 4);
INSERT INTO `product_sku` VALUES ('331567578151750', '6141515e34d325482391094516a31295', '1768137135178', 19.90, 98, 0);
INSERT INTO `product_sku` VALUES ('331567578151750', '8632a7a0dad33728d0afc2b16351f161', '1768137187569', 29.90, 100, 1);
INSERT INTO `product_sku` VALUES ('378919755916188', '19b67904c8d6319a7ea2451e75a15cfc', '1768106510018-1768106374324', 499.00, 200, 2);
INSERT INTO `product_sku` VALUES ('378919755916188', '304dc9239c6d6d996df4abc3b8c6bdcb', '1768106511697-1768106374324', 899.00, 100, 4);
INSERT INTO `product_sku` VALUES ('378919755916188', '7328104c0c7f9a3dd670b6685718f9da', '1768106510018-1768106553530', 899.00, 100, 3);
INSERT INTO `product_sku` VALUES ('378919755916188', '7b534fe4f948575522666a7c31945752', '1768106374323-1768106553530', 499.00, 200, 1);
INSERT INTO `product_sku` VALUES ('378919755916188', 'b021f01e05424e0bc12f17f06eb9ebcf', '1768106374323-1768106374324', 499.00, 200, 0);
INSERT INTO `product_sku` VALUES ('378919755916188', 'e1d2d1d27d211c77fe047bd9a4889153', '1768106511697-1768106553530', 899.00, 100, 5);
INSERT INTO `product_sku` VALUES ('422543322296606', '0372cce34c5f7db36532c7278801bd1f', '1768048730829-1768048758813', 39.90, 200, 12);
INSERT INTO `product_sku` VALUES ('422543322296606', '0423c9cf30525dc15042df72c640606c', '1768048731301-1768048766773', 39.90, 550, 19);
INSERT INTO `product_sku` VALUES ('422543322296606', '0c823fef545a90d294ef0ce1b8faf88a', '1768048730829-1768048759629', 39.90, 200, 13);
INSERT INTO `product_sku` VALUES ('422543322296606', '1145b6fbf50921920f393b8fa6e54d7f', '1768048730485-1768048758373', 39.90, 200, 6);
INSERT INTO `product_sku` VALUES ('422543322296606', '1374c0528345d0108cf5b2b9110ecd0e', '1768048731301-1768048758373', 39.90, 1111, 16);
INSERT INTO `product_sku` VALUES ('422543322296606', '194cf1f1ff07e42231bd5a06e076ebf5', '1768048731301-1768048722328', 39.90, 200, 15);
INSERT INTO `product_sku` VALUES ('422543322296606', '25ad09e5d79424e316290ab6a2b3071c', '1768048730829-1768048722328', 39.90, 200, 10);
INSERT INTO `product_sku` VALUES ('422543322296606', '35a1d95a52bfc1c651b31fc35687e2eb', '1768048730485-1768048766773', 39.90, 200, 9);
INSERT INTO `product_sku` VALUES ('422543322296606', '3941978129ab6f892a75020e68ed848f', '1768048731301-1768048758813', 39.90, 0, 17);
INSERT INTO `product_sku` VALUES ('422543322296606', '4a3ae15a0392d395fc8049f3ce7ddbf2', '1768048722327-1768048758813', 39.90, 200, 2);
INSERT INTO `product_sku` VALUES ('422543322296606', '5de06d8271580ee7000ad26053da42b0', '1768048730829-1768048758373', 39.90, 200, 11);
INSERT INTO `product_sku` VALUES ('422543322296606', '63a2ff53f186ecdad3a0c95e56b310d1', '1768048730829-1768048766773', 39.90, 200, 14);
INSERT INTO `product_sku` VALUES ('422543322296606', '845344c667ca34de3f6dbc66daff3534', '1768048722327-1768048759629', 39.90, 200, 3);
INSERT INTO `product_sku` VALUES ('422543322296606', '8779278d8f9b476c59838306819193ea', '1768048730485-1768048758813', 39.90, 200, 7);
INSERT INTO `product_sku` VALUES ('422543322296606', '9ec3af9caf9d2507f15d60764bbdd2c0', '1768048730485-1768048759629', 39.90, 200, 8);
INSERT INTO `product_sku` VALUES ('422543322296606', 'd71d43a3fe1aa0ef4a03c1c3ba879b59', '1768048722327-1768048722328', 39.90, 200, 0);
INSERT INTO `product_sku` VALUES ('422543322296606', 'd86a5c6f8a30d4272c7fa582176c8d43', '1768048722327-1768048766773', 39.90, 200, 4);
INSERT INTO `product_sku` VALUES ('422543322296606', 'dcc79cedee9b5f35358a6b2fbfe5bd47', '1768048731301-1768048759629', 39.90, 1000, 18);
INSERT INTO `product_sku` VALUES ('422543322296606', 'edcaf2d83902a1e0589127bde46fcbe0', '1768048722327-1768048758373', 39.90, 200, 1);
INSERT INTO `product_sku` VALUES ('422543322296606', 'f59ff174ba4aec568e033f616fe0fb8f', '1768048730485-1768048722328', 39.90, 200, 5);
INSERT INTO `product_sku` VALUES ('423205878567931', '459bc02d74300be86e5bceafcabbd914', '1768106208682-1768106233346', 2099.00, 0, 3);
INSERT INTO `product_sku` VALUES ('423205878567931', '76a40918eae0e940ac22f30eef62f8c6', '1768106208682-1768106050740', 2099.00, 300, 2);
INSERT INTO `product_sku` VALUES ('423205878567931', '8ee05b3735ebb3ff78f88be8e2c09108', '1768106050739-1768106233346', 1899.00, 300, 1);
INSERT INTO `product_sku` VALUES ('423205878567931', '9b70f666ba2e70e21e69c164c6cc5560', '1768106050739-1768106050740', 1899.00, 300, 0);
INSERT INTO `product_sku` VALUES ('438316828084252', '1e0045d66effc7214b82dd4c4b9cf773', '1768053085531', 89.90, 100, 1);
INSERT INTO `product_sku` VALUES ('438316828084252', 'f4fc7960f18634140bb3c39919aeddb9', '1768052963764', 39.90, 98, 0);
INSERT INTO `product_sku` VALUES ('531629223245423', '093b3032d7252790576a15386754d338', '1768048393199-1768048435869', 49.90, 200, 3);
INSERT INTO `product_sku` VALUES ('531629223245423', '0a409207139d8a87046f83517090af2e', '1768048393199-1768048436157', 49.90, 200, 4);
INSERT INTO `product_sku` VALUES ('531629223245423', '21c1409130c17c42c93b637a7d5ca633', '1768048397525-1768048393200', 49.90, 200, 10);
INSERT INTO `product_sku` VALUES ('531629223245423', '2c2269810bd85ec6a20a91c423c33078', '1768048397045-1768048435085', 49.90, 200, 6);
INSERT INTO `product_sku` VALUES ('531629223245423', '33671f54147c0e529b6c35c33f98d8c6', '1768048397045-1768048436157', 49.90, 200, 9);
INSERT INTO `product_sku` VALUES ('531629223245423', '37dcd7351ad3a90dc923869b20ca8e07', '1768048397045-1768048435869', 49.90, 200, 8);
INSERT INTO `product_sku` VALUES ('531629223245423', '55a06cb451ad912bb36eeff91d97bd42', '1768048397525-1768048435085', 49.90, 200, 11);
INSERT INTO `product_sku` VALUES ('531629223245423', '8fbfb9bd9a7c91a3735296b6801b9d59', '1768048397045-1768048435461', 49.90, 200, 7);
INSERT INTO `product_sku` VALUES ('531629223245423', '9be377dd25e74b821656b16a9139e9d3', '1768048393199-1768048393200', 49.90, 200, 0);
INSERT INTO `product_sku` VALUES ('531629223245423', 'a44d2b89e004f49e16928a28635b7f0d', '1768048393199-1768048435461', 49.90, 200, 2);
INSERT INTO `product_sku` VALUES ('531629223245423', 'a78c5170b2e0f890fed1fcf6436a3691', '1768048397045-1768048393200', 49.90, 200, 5);
INSERT INTO `product_sku` VALUES ('531629223245423', 'abbcd579b4b466fb9fa5093385d3cd73', '1768048397525-1768048435869', 49.90, 200, 13);
INSERT INTO `product_sku` VALUES ('531629223245423', 'b6fb1aee230609b558d9d9b2f18c715d', '1768048397525-1768048436157', 49.90, 200, 14);
INSERT INTO `product_sku` VALUES ('531629223245423', 'c4477f9e6455b169147466757a36514a', '1768048397525-1768048435461', 49.90, 200, 12);
INSERT INTO `product_sku` VALUES ('531629223245423', 'f9f5167e3da06d1359acd26551462d85', '1768048393199-1768048435085', 49.90, 200, 1);
INSERT INTO `product_sku` VALUES ('535739147419699', '491cdac6af7ba9547db45db87ef3aab8', '1768051305653-1768051305654', 10.90, 200, 0);
INSERT INTO `product_sku` VALUES ('535739147419699', '5958865272ce9e0e678fa284501f0fb6', '1768051377724-1768051305654', 10.90, 200, 2);
INSERT INTO `product_sku` VALUES ('535739147419699', '89c76aa401da9a289597e29d7f20336a', '1768051305653-1768051397428', 9.90, 199, 1);
INSERT INTO `product_sku` VALUES ('535739147419699', 'fef16169390995f8fbae6cd66e417fac', '1768051377724-1768051397428', 9.90, 200, 3);
INSERT INTO `product_sku` VALUES ('569096788843148', '588a56605f76330781eddaa3df0712f0', '1768052200558-1768052200559', 59.90, 198, 0);
INSERT INTO `product_sku` VALUES ('569096788843148', 'af6f3bdc76263ea73427046ef9c6e843', '1768052200558-1768052244044', 89.90, 200, 1);
INSERT INTO `product_sku` VALUES ('664740861226404', '94234e67719428840e97e1aa60d08ca4', '1768126444657', 59.90, 1000, 0);
INSERT INTO `product_sku` VALUES ('685733118738049', '094144b94c3dae3b8fc551312ac3e88c', '1768136388210-1768136476225', 399.00, 100, 1);
INSERT INTO `product_sku` VALUES ('685733118738049', '121b9dce0ac8085bd3c26a514a590752', '1768136388210-1768136485609', 399.00, 100, 3);
INSERT INTO `product_sku` VALUES ('685733118738049', '2f57cf8d1ee93b5df4e4a3d69dcbd0e1', '1768136388210-1768136388211', 399.00, 100, 0);
INSERT INTO `product_sku` VALUES ('685733118738049', '432a2efe234f33493baf3e3cea9f3c17', '1768136464497-1768136476616', 399.00, 100, 6);
INSERT INTO `product_sku` VALUES ('685733118738049', '73ba017e7320c700d3013201c866144b', '1768136464497-1768136476225', 399.00, 100, 5);
INSERT INTO `product_sku` VALUES ('685733118738049', '9803f077b0ce12c383db8229dcdd3d66', '1768136464497-1768136388211', 399.00, 100, 4);
INSERT INTO `product_sku` VALUES ('685733118738049', '9ecd3e5e1ce2d1cd6791b32ccaca55de', '1768136388210-1768136476616', 399.00, 100, 2);
INSERT INTO `product_sku` VALUES ('685733118738049', 'b5ee5c6ba549c4aaa03e16f08d97d4db', '1768136464497-1768136485609', 399.00, 100, 7);
INSERT INTO `product_sku` VALUES ('710762418261843', '0c05b3e68beefab8c0d5d38b60fc4926', '1768127887012', 200.00, 1000, 2);
INSERT INTO `product_sku` VALUES ('710762418261843', '47f6bdbba8b38bb1b4ef14fb10020b0b', '1768127890222', 200.00, 1000, 3);
INSERT INTO `product_sku` VALUES ('710762418261843', '55ec81e272686e38e22a1e53e03d0488', '1768127788649', 200.00, 998, 0);
INSERT INTO `product_sku` VALUES ('710762418261843', '55fb3201b4a042fdaa40d7482101db49', '1768127883444', 200.00, 1000, 1);
INSERT INTO `product_sku` VALUES ('710762418261843', 'b325d5319fe949021fb6e3a691347e78', '1768127896878', 200.00, 1000, 4);
INSERT INTO `product_sku` VALUES ('746070730569292', '3820a9bd715f175d1cdb80fa262d2876', '1768033276450', 12.80, 999, 0);
INSERT INTO `product_sku` VALUES ('746070730569292', '5279179788effba79dbcee256703d697', '1768033372171', 12.80, 1000, 1);
INSERT INTO `product_sku` VALUES ('746070730569292', '5b6b9e924c7197684f4fda7e58e2967c', '1768033471342', 12.80, 1000, 5);
INSERT INTO `product_sku` VALUES ('746070730569292', 'ac3f00144b4ba5167fb598b7e635923f', '1768033393748', 12.80, 1000, 2);
INSERT INTO `product_sku` VALUES ('746070730569292', 'cfac354d46ebfc10ef9e92002c6d9315', '1768033412137', 12.80, 999, 3);
INSERT INTO `product_sku` VALUES ('746070730569292', 'e2a3297e023f683b85261b516a295515', '1768033437679', 12.80, 1000, 4);
INSERT INTO `product_sku` VALUES ('754194826041867', '27eb86db0ad0ae347a3174f0215ffab0', '1768047696583-1768048038181', 79.90, 200, 2);
INSERT INTO `product_sku` VALUES ('754194826041867', '3333f818c9d029fd188dc32047003aea', '1768048005702-1768048035229', 79.90, 200, 4);
INSERT INTO `product_sku` VALUES ('754194826041867', '6caab33bde308e3d336759dc63145b89', '1768047696583-1768048035229', 79.90, 200, 1);
INSERT INTO `product_sku` VALUES ('754194826041867', '93ce36c33062a3b126e2c7fe3816d3d6', '1768047696583-1768047696584', 79.90, 199, 0);
INSERT INTO `product_sku` VALUES ('754194826041867', 'c7cc68e5115a1a258dffd213a566793e', '1768048005702-1768048038181', 79.90, 0, 5);
INSERT INTO `product_sku` VALUES ('754194826041867', 'feb190dd2871335ae4b6d54b9ba2edfd', '1768048005702-1768047696584', 79.90, 200, 3);
INSERT INTO `product_sku` VALUES ('756114288369968', 'a29d6e99d4d95a8a96e79783164de345', '1768050669517', 59.90, 198, 0);
INSERT INTO `product_sku` VALUES ('756114288369968', 'afb9e9132a33b5c8962c31918cee4b80', '1768050729196', 59.90, 200, 1);
INSERT INTO `product_sku` VALUES ('761711957636857', '1a0fbe4afe353734d5a033972a62b5aa', '1768049856622-1768049939892', 139.90, 200, 4);
INSERT INTO `product_sku` VALUES ('761711957636857', '1ae6efd4d15445df07082b0ecfd4b02f', '1768049882109-1768049931861', 139.90, 200, 12);
INSERT INTO `product_sku` VALUES ('761711957636857', '2da714a831198b83851530429a5d688a', '1768049856622-1768049856623', 139.90, 199, 0);
INSERT INTO `product_sku` VALUES ('761711957636857', '31916c15df271e4506a1a7a2df32f04e', '1768049889269-1768049939892', 139.90, 200, 19);
INSERT INTO `product_sku` VALUES ('761711957636857', '3d0682e494ed151b7ac4f273f28c2b80', '1768049856622-1768049931517', 139.90, 200, 1);
INSERT INTO `product_sku` VALUES ('761711957636857', '465c09ec8612679f6311cf3204756a98', '1768049889269-1768049931861', 139.90, 200, 17);
INSERT INTO `product_sku` VALUES ('761711957636857', '5126e18b05b7f0ae321d245c037d890b', '1768049856622-1768049932292', 139.90, 200, 3);
INSERT INTO `product_sku` VALUES ('761711957636857', '5aeef71aa3c5a4f78783d31b33232f27', '1768049889573-1768049932292', 139.90, 200, 23);
INSERT INTO `product_sku` VALUES ('761711957636857', '61addf258f55d8854a954f3ec77733cd', '1768049889269-1768049856623', 139.90, 200, 15);
INSERT INTO `product_sku` VALUES ('761711957636857', '6b49fa8f7f8fb3dc9631f92478253aa4', '1768049881653-1768049856623', 139.90, 200, 5);
INSERT INTO `product_sku` VALUES ('761711957636857', '6cacae4ce5976a67cb132ca986380c19', '1768049882109-1768049856623', 139.90, 200, 10);
INSERT INTO `product_sku` VALUES ('761711957636857', '7a87ca9395aa7de2342055cc6732c827', '1768049889573-1768049931861', 139.90, 200, 22);
INSERT INTO `product_sku` VALUES ('761711957636857', '8188d7279dd5d77e53897d5d8bf9ccb2', '1768049881653-1768049931517', 139.90, 200, 6);
INSERT INTO `product_sku` VALUES ('761711957636857', '84083917cf750ec9c71fb1567a76e48e', '1768049882109-1768049932292', 139.90, 200, 13);
INSERT INTO `product_sku` VALUES ('761711957636857', '84869dc1fd1f3abe78424da2a4ed18fd', '1768049889573-1768049856623', 139.90, 200, 20);
INSERT INTO `product_sku` VALUES ('761711957636857', 'a1c7f6cf6155e03d4aa2cd07c838f8d7', '1768049889573-1768049939892', 139.90, 200, 24);
INSERT INTO `product_sku` VALUES ('761711957636857', 'a4361736e0bc81a4fc85232fee3d82a4', '1768049881653-1768049939892', 139.90, 200, 9);
INSERT INTO `product_sku` VALUES ('761711957636857', 'aaf7a7f4c6ce3438cf163eb2b92f7899', '1768049856622-1768049931861', 139.90, 200, 2);
INSERT INTO `product_sku` VALUES ('761711957636857', 'b01596c69d2269ead22fbc88fe15decd', '1768049881653-1768049932292', 139.90, 200, 8);
INSERT INTO `product_sku` VALUES ('761711957636857', 'ca7461ac8da44a130c817c31d232223a', '1768049889573-1768049931517', 139.90, 200, 21);
INSERT INTO `product_sku` VALUES ('761711957636857', 'd5574be7e3335588e5d1ff61e430d2b8', '1768049881653-1768049931861', 139.90, 200, 7);
INSERT INTO `product_sku` VALUES ('761711957636857', 'e107d92eddbe45ce86038472079ad759', '1768049882109-1768049931517', 139.90, 200, 11);
INSERT INTO `product_sku` VALUES ('761711957636857', 'e2041f7b703e6c79c9b6aceb8d2723c7', '1768049889269-1768049931517', 139.90, 200, 16);
INSERT INTO `product_sku` VALUES ('761711957636857', 'eb89b1c8652fc3c4bb7f5dca8f05bc24', '1768049889269-1768049932292', 139.90, 200, 18);
INSERT INTO `product_sku` VALUES ('761711957636857', 'f2060e4d17108454918dc8f37f192563', '1768049882109-1768049939892', 139.90, 200, 14);
INSERT INTO `product_sku` VALUES ('763086281772264', '2675cb5d7172c3c5ff07c8c9811f71c7', '1768050429180', 19.90, 299, 1);
INSERT INTO `product_sku` VALUES ('763086281772264', '4408ece52af09cc10aa035676bc4f668', '1768050433492', 35.90, 300, 2);
INSERT INTO `product_sku` VALUES ('763086281772264', '75efb5191c7da469fcc570aba1e21041', '1768050383077', 19.90, 300, 0);
INSERT INTO `product_sku` VALUES ('767781456711238', '0d69a09a95005935cf518e9efb6436e0', '1768049441525-1768049473437', 89.90, 200, 5);
INSERT INTO `product_sku` VALUES ('767781456711238', '20b7c6454c871550f7e0e6344f33e6cd', '1768049438134-1768049438135', 89.90, 200, 0);
INSERT INTO `product_sku` VALUES ('767781456711238', '303e4e4e0a6ad43b74d7354c3c982d4d', '1768049441525-1768049438135', 89.90, 200, 3);
INSERT INTO `product_sku` VALUES ('767781456711238', '3544291348dea3722406d927ec905dc8', '1768049443357-1768049473437', 89.90, 200, 14);
INSERT INTO `product_sku` VALUES ('767781456711238', '55a363b5b9ebac83a100ca0d0949eef1', '1768049443357-1768049472973', 89.90, 200, 13);
INSERT INTO `product_sku` VALUES ('767781456711238', '667fd0d7164097170e03217e5b852709', '1768049441525-1768049472973', 89.90, 200, 4);
INSERT INTO `product_sku` VALUES ('767781456711238', '67cfe9018f861fa65a6d27c490534358', '1768049442356-1768049473437', 89.90, 200, 8);
INSERT INTO `product_sku` VALUES ('767781456711238', '73d3ceeed1ccbcbf21f0691ebedb5d08', '1768049442356-1768049438135', 89.90, 200, 6);
INSERT INTO `product_sku` VALUES ('767781456711238', 'a1baae9404390396432d55c141d61e06', '1768049443357-1768049438135', 89.90, 200, 12);
INSERT INTO `product_sku` VALUES ('767781456711238', 'a21183df751be8cb4eb22ca288687c98', '1768049442356-1768049472973', 89.90, 200, 7);
INSERT INTO `product_sku` VALUES ('767781456711238', 'ae0364d69a636c9afed4a618f5d4a1ae', '1768049442829-1768049472973', 89.90, 200, 10);
INSERT INTO `product_sku` VALUES ('767781456711238', 'afd224e5f6f59c589443fbd23306661f', '1768049438134-1768049473437', 89.90, 200, 2);
INSERT INTO `product_sku` VALUES ('767781456711238', 'c8c7be4fd6c22127ddf7b3c90cfe1f4f', '1768049438134-1768049472973', 89.90, 200, 1);
INSERT INTO `product_sku` VALUES ('767781456711238', 'dcd0f3b0bc114af568a38a066769c6a1', '1768049442829-1768049438135', 89.90, 200, 9);
INSERT INTO `product_sku` VALUES ('767781456711238', 'eddaab5a911bb43ffe8c26c28efe7d5a', '1768049442829-1768049473437', 89.90, 199, 11);
INSERT INTO `product_sku` VALUES ('768845804636453', '33e25c7bc4660e6848918b054419f753', '1768052716419-1768052752308', 49.90, 200, 3);
INSERT INTO `product_sku` VALUES ('768845804636453', '415e97bcecd7be8c1e50369b91ffffc8', '1768052687260-1768052687261', 29.90, 199, 0);
INSERT INTO `product_sku` VALUES ('768845804636453', '82e29dc6ca0bd2e17f8a92b3c2192f8a', '1768052716419-1768052687261', 29.90, 200, 2);
INSERT INTO `product_sku` VALUES ('768845804636453', '88f2dfdcaa15a0c2d9050db25802e9e3', '1768052687260-1768052752308', 49.90, 200, 1);
INSERT INTO `product_sku` VALUES ('843304724668395', '1ee624ffb7730f00d8db3417d310ec3d', '1768052450716-1768052450717', 19.90, 999, 0);
INSERT INTO `product_sku` VALUES ('843304724668395', '4494ac8930bde6ac73125e19a1bd1910', '1768052488467-1768052450717', 19.90, 1000, 1);
INSERT INTO `product_sku` VALUES ('864824304719236', '19cc0f65d89b5789d661b787400958b8', '1768049128413-1768049161189', 189.90, 300, 7);
INSERT INTO `product_sku` VALUES ('864824304719236', '8b7da0343b63087230e07e7ca97f6ab6', '1768049124430-1768049160885', 189.90, 300, 1);
INSERT INTO `product_sku` VALUES ('864824304719236', '9562078f6a22e5d45021e777611279b4', '1768049124430-1768049168829', 189.90, 300, 4);
INSERT INTO `product_sku` VALUES ('864824304719236', '9f34d88dc6b855752ccbe23c4709b74a', '1768049128413-1768049160885', 189.90, 300, 6);
INSERT INTO `product_sku` VALUES ('864824304719236', 'a926fe64ddd163da5d555efdca51d15f', '1768049124430-1768049124431', 189.90, 299, 0);
INSERT INTO `product_sku` VALUES ('864824304719236', 'bd8d2ef6ebada96a9b80e7e678c73084', '1768049124430-1768049161189', 189.90, 300, 2);
INSERT INTO `product_sku` VALUES ('864824304719236', 'c9164bf5e48a5624fb24a786dfbc053d', '1768049124430-1768049161605', 189.90, 300, 3);
INSERT INTO `product_sku` VALUES ('864824304719236', 'd132f2253a2b8fde3a441d8d502d4c86', '1768049128413-1768049124431', 189.90, 300, 5);
INSERT INTO `product_sku` VALUES ('864824304719236', 'ec0f39fc0fa9dff67dd5c8c1d94ef8b8', '1768049128413-1768049161605', 189.90, 300, 8);
INSERT INTO `product_sku` VALUES ('864824304719236', 'f8c58ccab5cee51284aec41a73576a3f', '1768049128413-1768049168829', 189.90, 300, 9);
INSERT INTO `product_sku` VALUES ('917186661226040', '444ac3778c4754b97944f5f3a0e23fbc', '1768125478450', 25.90, 100, 0);
INSERT INTO `product_sku` VALUES ('917186661226040', 'ec54a3c0108ce143bf0b6542e29ec750', '1768125501664', 19.90, 100, 1);
INSERT INTO `product_sku` VALUES ('996249180877578', '0a16aaec02af6e37ea77853a2980d96d', '1768136660947-1768136660948', 299.00, 200, 0);
INSERT INTO `product_sku` VALUES ('996249180877578', '38856095eaa0891229adab06f6741377', '1768136672345-1768136660948', 299.00, 200, 4);
INSERT INTO `product_sku` VALUES ('996249180877578', '79f40dd618fdacd709ec68c861c03ed4', '1768136660947-1768136704993', 299.00, 200, 2);
INSERT INTO `product_sku` VALUES ('996249180877578', '8f4dad3f0f431e6051615ee00d3816ad', '1768136660947-1768136704401', 299.00, 200, 1);
INSERT INTO `product_sku` VALUES ('996249180877578', '98dbe33a53d1ff81d2a13e85f4b856cd', '1768136672345-1768136704993', 299.00, 200, 6);
INSERT INTO `product_sku` VALUES ('996249180877578', 'b19eb8da242da5ae4b8354723f6284bf', '1768136672345-1768136714065', 299.00, 200, 7);
INSERT INTO `product_sku` VALUES ('996249180877578', 'cd1899002771d28ec179324088418359', '1768136660947-1768136714065', 299.00, 200, 3);
INSERT INTO `product_sku` VALUES ('996249180877578', 'f575567548837a19be37b86ebdf62d55', '1768136672345-1768136704401', 299.00, 200, 5);

-- ----------------------------
-- Table structure for rag_question
-- ----------------------------
DROP TABLE IF EXISTS `rag_question`;
CREATE TABLE `rag_question`  (
  `question_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `question` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '问题',
  `similar_question` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相似问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '答案',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`question_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'rag问题' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of rag_question
-- ----------------------------
INSERT INTO `rag_question` VALUES (4, '是否支持7天无理由', NULL, '本商城的特殊性，不支持7天无理由，确认收货后无法退款，请您谅解。', '2026-01-09 12:24:42');
INSERT INTO `rag_question` VALUES (5, '你是谁开发的？', '[\"谁创造的你\",\"谁开发了你\"]', '我是程序员老罗开发的智能购物助手AI，致力于为用户提供便捷、高效的电商服务。如果您有任何购物相关的问题，欢迎随时向我提问，我将竭诚为您服务。', '2026-01-09 14:19:27');
INSERT INTO `rag_question` VALUES (6, '程序员老罗是谁？', '[\"你的作者是谁\"]', '程序员老罗是一位很优秀的up主，技术精湛，代表作有 网盘，easyChat，仿B站，仿腾讯会议，AI音乐等。\r\n老罗的B站主页是  https://space.bilibili.com/499388891\r\n老罗的帅照![](/api/file/getResource?sourceName=202601/Cm07U1MnpSJaiV2SXAn7pqgHJixwdY.png)\r\n', '2026-01-09 14:20:46');
INSERT INTO `rag_question` VALUES (7, '你是谁？', '[\"你能干什么\"]', '你是程序员老罗开发的智能购物助手AI，负责为用户提供全流程电商服务。', '2026-01-10 15:20:19');

-- ----------------------------
-- Table structure for statistics_info
-- ----------------------------
DROP TABLE IF EXISTS `statistics_info`;
CREATE TABLE `statistics_info`  (
  `statistics_date` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日期',
  `data_type` tinyint(1) NOT NULL COMMENT '数据类型',
  `data_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '统计数据',
  PRIMARY KEY (`statistics_date`, `data_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据统计结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of statistics_info
-- ----------------------------
INSERT INTO `statistics_info` VALUES ('2026-01-03', 1, 200.00);
INSERT INTO `statistics_info` VALUES ('2026-01-03', 2, 50.00);
INSERT INTO `statistics_info` VALUES ('2026-01-03', 3, 15.00);
INSERT INTO `statistics_info` VALUES ('2026-01-03', 4, 1.00);
INSERT INTO `statistics_info` VALUES ('2026-01-04', 1, 179.80);
INSERT INTO `statistics_info` VALUES ('2026-01-04', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-04', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-04', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-05', 1, 99.80);
INSERT INTO `statistics_info` VALUES ('2026-01-05', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-05', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-05', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-06', 1, 59.90);
INSERT INTO `statistics_info` VALUES ('2026-01-06', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-06', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-06', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-07', 1, 359.50);
INSERT INTO `statistics_info` VALUES ('2026-01-07', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-07', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-07', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-08', 1, 262.60);
INSERT INTO `statistics_info` VALUES ('2026-01-08', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-08', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-08', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-09', 1, 95.70);
INSERT INTO `statistics_info` VALUES ('2026-01-09', 2, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-09', 3, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-09', 4, 0.00);
INSERT INTO `statistics_info` VALUES ('2026-01-10', 1, 98.50);
INSERT INTO `statistics_info` VALUES ('2026-01-10', 2, 1.00);
INSERT INTO `statistics_info` VALUES ('2026-01-10', 3, 12.00);
INSERT INTO `statistics_info` VALUES ('2026-01-10', 4, 1.00);
INSERT INTO `statistics_info` VALUES ('2026-01-11', 1, 200.00);
INSERT INTO `statistics_info` VALUES ('2026-01-11', 2, 1.00);
INSERT INTO `statistics_info` VALUES ('2026-01-11', 3, 50.00);
INSERT INTO `statistics_info` VALUES ('2026-01-11', 4, 2.00);
INSERT INTO `statistics_info` VALUES ('2026-01-13', 1, 900.00);
INSERT INTO `statistics_info` VALUES ('2026-01-13', 2, 20.00);
INSERT INTO `statistics_info` VALUES ('2026-01-13', 3, 60.00);
INSERT INTO `statistics_info` VALUES ('2026-01-13', 4, 2.00);

-- ----------------------------
-- Table structure for sys_category
-- ----------------------------
DROP TABLE IF EXISTS `sys_category`;
CREATE TABLE `sys_category`  (
  `category_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `p_category_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0',
  `sort` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_category
-- ----------------------------
INSERT INTO `sys_category` VALUES ('10001', '数码家电', '0', 1);
INSERT INTO `sys_category` VALUES ('10002', '服装鞋帽', '0', 2);
INSERT INTO `sys_category` VALUES ('10003', '美妆个护', '0', 3);
INSERT INTO `sys_category` VALUES ('10004', '家居生活', '0', 4);
INSERT INTO `sys_category` VALUES ('10005', '食品生鲜', '0', 5);
INSERT INTO `sys_category` VALUES ('10006', '文体娱乐', '0', 6);
INSERT INTO `sys_category` VALUES ('10007', '汽车用品', '0', 7);
INSERT INTO `sys_category` VALUES ('10011', '虚拟产品', '0', 11);
INSERT INTO `sys_category` VALUES ('20001', '手机通讯', '10001', 1);
INSERT INTO `sys_category` VALUES ('20002', '电脑办公', '10001', 2);
INSERT INTO `sys_category` VALUES ('20003', '数码影音', '10001', 3);
INSERT INTO `sys_category` VALUES ('20004', '家用电器', '10001', 4);
INSERT INTO `sys_category` VALUES ('20007', '女装', '10002', 1);
INSERT INTO `sys_category` VALUES ('20008', '男装', '10002', 2);
INSERT INTO `sys_category` VALUES ('20009', '运动户外', '10002', 3);
INSERT INTO `sys_category` VALUES ('20010', '鞋靴箱包', '10002', 4);
INSERT INTO `sys_category` VALUES ('20011', '内衣配饰', '10002', 5);
INSERT INTO `sys_category` VALUES ('20012', '美妆护肤', '10003', 1);
INSERT INTO `sys_category` VALUES ('20013', '个人护理', '10003', 2);
INSERT INTO `sys_category` VALUES ('20014', '母婴用品', '10003', 3);
INSERT INTO `sys_category` VALUES ('20015', '香水彩妆', '10003', 4);
INSERT INTO `sys_category` VALUES ('20016', '家具家装', '10004', 1);
INSERT INTO `sys_category` VALUES ('20017', '家居家纺', '10004', 2);
INSERT INTO `sys_category` VALUES ('20018', '厨具餐具', '10004', 3);
INSERT INTO `sys_category` VALUES ('20019', '家居饰品', '10004', 4);
INSERT INTO `sys_category` VALUES ('20020', '收纳清洁', '10004', 5);
INSERT INTO `sys_category` VALUES ('20021', '生鲜食品', '10005', 1);
INSERT INTO `sys_category` VALUES ('20022', '休闲食品', '10005', 2);
INSERT INTO `sys_category` VALUES ('20023', '酒水饮料', '10005', 3);
INSERT INTO `sys_category` VALUES ('20024', '粮油调味', '10005', 4);
INSERT INTO `sys_category` VALUES ('20025', '滋补保健', '10005', 5);
INSERT INTO `sys_category` VALUES ('20026', '图书文娱', '10006', 1);
INSERT INTO `sys_category` VALUES ('20027', '运动健身', '10006', 2);
INSERT INTO `sys_category` VALUES ('20028', '玩具乐器', '10006', 3);
INSERT INTO `sys_category` VALUES ('20029', '办公设备', '10006', 4);
INSERT INTO `sys_category` VALUES ('20030', '汽车配件', '10007', 1);
INSERT INTO `sys_category` VALUES ('20031', '汽车装饰', '10007', 2);
INSERT INTO `sys_category` VALUES ('20032', '维修保养', '10007', 3);
INSERT INTO `sys_category` VALUES ('20033', '车载电器', '10007', 4);
INSERT INTO `sys_category` VALUES ('20044', '在线课程', '10011', 1);
INSERT INTO `sys_category` VALUES ('20045', '软件服务', '10011', 2);
INSERT INTO `sys_category` VALUES ('20046', '会员服务', '10011', 3);
INSERT INTO `sys_category` VALUES ('20047', '游戏点卡', '10011', 4);
INSERT INTO `sys_category` VALUES ('64617', '其他', '0', 12);
INSERT INTO `sys_category` VALUES ('88409', '其他', '64617', 1);

-- ----------------------------
-- Table structure for sys_product_property
-- ----------------------------
DROP TABLE IF EXISTS `sys_product_property`;
CREATE TABLE `sys_product_property`  (
  `property_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性ID',
  `property_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性名称',
  `p_category_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '一级分类',
  `category_id` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '二级分类',
  `property_sort` int(11) NULL DEFAULT NULL COMMENT '排序',
  `cover_type` tinyint(1) NULL DEFAULT NULL COMMENT '0:无需传封面 1:需传封面',
  PRIMARY KEY (`property_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_product_property
-- ----------------------------
INSERT INTO `sys_product_property` VALUES ('1001', '颜色', '10001', '20001', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1004', '颜色', '10001', '20002', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1007', '颜色', '10001', '20003', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1008', '型号', '10001', '20003', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1009', '存储容量', '10001', '20003', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1010', '颜色', '10001', '20004', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1011', '容量', '10001', '20004', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1012', '型号', '10001', '20004', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1013', '颜色', '10001', '20005', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1014', '容量', '10001', '20005', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1015', '功率', '10001', '20005', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1016', '颜色', '10001', '20006', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1017', '型号', '10001', '20006', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1018', '功能配置', '10001', '20006', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1019', '颜色', '10002', '20007', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1020', '尺码', '10002', '20007', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1022', '颜色', '10002', '20008', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1023', '尺码', '10002', '20008', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1025', '颜色', '10002', '20009', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1026', '尺码', '10002', '20009', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1027', '防护级别', '10002', '20009', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1028', '颜色', '10002', '20010', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1029', '尺码', '10002', '20010', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1031', '颜色', '10002', '20011', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1034', '颜色分类', '10003', '20012', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1037', '型号', '10003', '20013', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1038', '颜色', '10003', '20013', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1039', '刀头数量', '10003', '20013', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1040', '尺码', '10003', '20014', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1041', '颜色', '10003', '20014', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1042', '段位', '10003', '20014', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1043', '香型', '10003', '20015', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1044', '容量', '10003', '20015', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1045', '套装', '10003', '20015', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1046', '颜色', '10004', '20016', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1048', '尺寸', '10004', '20016', 3, 0);
INSERT INTO `sys_product_property` VALUES ('1049', '颜色', '10004', '20017', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1050', '尺寸', '10004', '20017', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1052', '颜色', '10004', '20018', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1053', '尺寸', '10004', '20018', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1054', '容量', '10004', '20018', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1055', '颜色', '10004', '20019', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1056', '尺寸', '10004', '20019', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1057', '款式', '10004', '20019', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1058', '颜色', '10004', '20020', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1059', '尺寸', '10004', '20020', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1060', '容量', '10004', '20020', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1061', '口味', '10005', '20021', 1, 1);
INSERT INTO `sys_product_property` VALUES ('1064', '口味', '10005', '20022', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1067', '容量', '10005', '20023', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1068', '年份', '10005', '20023', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1069', '包装', '10005', '20023', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1070', '净含量', '10005', '20024', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1071', '包装', '10005', '20024', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1072', '等级', '10005', '20024', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1073', '规格', '10005', '20025', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1074', '净含量', '10005', '20025', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1075', '包装', '10005', '20025', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1076', '版本', '10006', '20026', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1077', '装帧', '10006', '20026', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1078', '套装', '10006', '20026', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1079', '颜色', '10006', '20027', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1080', '尺寸', '10006', '20027', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1081', '重量级别', '10006', '20027', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1082', '颜色', '10006', '20028', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1083', '尺寸', '10006', '20028', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1084', '型号', '10006', '20028', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1085', '颜色', '10006', '20029', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1086', '型号', '10006', '20029', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1087', '配置', '10006', '20029', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1088', '型号', '10007', '20030', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1089', '规格', '10007', '20030', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1090', '适用车型', '10007', '20030', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1091', '颜色', '10007', '20031', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1092', '尺寸', '10007', '20031', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1093', '款式', '10007', '20031', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1094', '型号', '10007', '20032', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1095', '规格', '10007', '20032', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1096', '容量', '10007', '20032', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1097', '颜色', '10007', '20033', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1098', '型号', '10007', '20033', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1099', '功率', '10007', '20033', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1100', '材质', '10008', '20034', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1101', '重量', '10008', '20034', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1102', '尺寸', '10008', '20034', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1103', '颜色', '10008', '20035', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1104', '材质', '10008', '20035', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1105', '尺寸', '10008', '20035', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1106', '型号', '10008', '20036', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1107', '表带材质', '10008', '20036', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1108', '表盘尺寸', '10008', '20036', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1109', '颜色', '10008', '20037', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1110', '尺寸', '10008', '20037', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1111', '款式', '10008', '20037', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1112', '颜色', '10009', '20038', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1113', '尺寸', '10009', '20038', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1114', '包装', '10009', '20038', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1115', '颜色', '10009', '20039', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1116', '尺寸', '10009', '20039', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1117', '花材', '10009', '20039', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1118', '颜色', '10009', '20040', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1119', '尺寸', '10009', '20040', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1120', '款式', '10009', '20040', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1121', '型号', '10010', '20041', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1122', '规格', '10010', '20041', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1123', '适用部位', '10010', '20041', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1124', '规格', '10010', '20042', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1125', '容量', '10010', '20042', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1126', '包装', '10010', '20042', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1127', '规格', '10010', '20043', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1128', '净含量', '10010', '20043', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1129', '包装', '10010', '20043', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1130', '时长', '10011', '20044', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1131', '版本', '10011', '20044', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1132', '套餐', '10011', '20044', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1133', '版本', '10011', '20045', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1134', '授权数量', '10011', '20045', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1135', '服务期限', '10011', '20045', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1136', '时长', '10011', '20046', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1137', '等级', '10011', '20046', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1138', '套餐', '10011', '20046', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('1139', '面值', '10011', '20047', 1, NULL);
INSERT INTO `sys_product_property` VALUES ('1140', '数量', '10011', '20047', 2, NULL);
INSERT INTO `sys_product_property` VALUES ('1141', '版本', '10011', '20047', 3, NULL);
INSERT INTO `sys_product_property` VALUES ('44065', '颜色分类', '64617', '88409', NULL, 1);

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `address_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详细地址',
  `addressee` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收货人',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `default_type` tinyint(4) NULL DEFAULT NULL COMMENT '默认类型0:非默认  1:默认',
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_address
-- ----------------------------

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `sex` tinyint(1) NULL DEFAULT NULL COMMENT '0:女 1:男 2:未知',
  `join_time` datetime NOT NULL COMMENT '加入时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '0:禁用 1:正常',
  `api_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '大模型应用key',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `idx_key_email`(`email`) USING BTREE,
  UNIQUE INDEX `idx_nick_name`(`nick_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES ('2734405271', '程序员老罗', '202601/rtyk7swvJJvJE5xXrdRGi8pOP0TcVh_thumbnail.jpg', 'test@qq.com', '47ec2dd791e31e2ef2076caf64ed9b3d', 1, '2026-01-04 10:15:33', '2026-01-14 10:30:38', '0:0:0:0:0:0:0:1', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
