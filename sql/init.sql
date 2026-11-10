-- ============================================
-- BraisedPorkBelly - 数据库初始化脚本
-- ============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS braised_pork_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE braised_pork_db;

-- 2. 创建表

DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255) DEFAULT '/images/default-avatar.png',
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    street VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    stock INT DEFAULT 0,
    category VARCHAR(50),
    image_url VARCHAR(255),
    images TEXT,
    status VARCHAR(20) DEFAULT 'ON_SALE',
    sales_count BIGINT DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待支付, PAYMENT_REVIEW待确认支付, PAID待发货, SHIPPED已发货, COMPLETED已完成, CANCELLED已取消',
    address VARCHAR(255),
    phone VARCHAR(20),
    receiver VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 订单项表
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    product_price DECIMAL(10,2),
    quantity INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 插入测试数据

-- 管理员用户 (admin123)
INSERT INTO `user` (username, password, email, phone, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@braisedpork.com', '13800138000', 'ADMIN');

-- 8个腊肉商品示例数据
INSERT INTO product (name, description, price, original_price, stock, category, image_url, status, sales_count) VALUES
('湖南烟熏腊肉',
 '精选湖南传统工艺烟熏腊肉，采用柏树枝慢火熏制30天，肉质紧实，烟香浓郁，肥瘦相间，入口即化。适合切片蒸煮或爆炒，是湖南人年夜饭的必备佳肴。',
 68.00, 88.00, 500, '腊肉', '/images/products/hunan-smoked.jpg', 'ON_SALE', 2380),

('四川麻辣腊肠',
 '正宗四川麻辣腊肠，选用优质猪后腿肉，配以四川花椒、辣椒等天然香料灌制而成。麻辣鲜香，回味悠长，蒸熟切片即可食用，也可作为下酒小菜。',
 55.00, 72.00, 380, '腊肠', '/images/products/sichuan-sausage.jpg', 'ON_SALE', 1960),

('贵州风干腊肉',
 '贵州苗寨传统风干腊肉，经苗家古法腌制后自然风干，不添加任何防腐剂。肉质醇厚，带有独特的山野风味，脂肪透明如琥珀，瘦肉红润有光泽。',
 75.00, 98.00, 300, '腊肉', '/images/products/guizhou-dried.jpg', 'ON_SALE', 1520),

('云南火腿',
 '云南宣威火腿，中国三大名腿之一，选用乌金猪后腿，经三年以上发酵陈化。色泽鲜艳，香气扑鼻，瘦肉嫩而甜，肥而不腻，可生食切片，也可入菜煲汤。',
 128.00, 168.00, 200, '火腿', '/images/products/yunnan-ham.jpg', 'ON_SALE', 1350),

('湖北恩施土家腊肉',
 '恩施土家族传统腊肉，取自高海拔散养黑猪，用柏枝、茶壳、橘皮熏制而成。烟熏味醇厚自然，肉质细嫩，脂肪少，蛋白质含量高，是土家族招待贵客的上等佳品。',
 72.00, 95.00, 260, '腊肉', '/images/products/hubei-tujia.jpg', 'ON_SALE', 1180),

('广东腊肠',
 '广式腊肠，采用传统糖酒腌制工艺，选用新鲜猪前腿肉切丁灌制。甜中带酒香，色泽红润透亮，口感细腻软滑，蒸煮后香气四溢，是广东烧腊的经典代表。',
 48.00, 62.00, 600, '腊肠', '/images/products/guangdong-sausage.jpg', 'ON_SALE', 2650),

('重庆城口老腊肉',
 '城口老腊肉是重庆非物质文化遗产，选用大巴山散养土猪，经20余种天然香料腌制，再以青冈木慢火熏烤45天以上。腊香醇厚，油而不腻，越嚼越香，有"一里城口腊肉香"的美誉。',
 88.00, 118.00, 180, '腊肉', '/images/products/chongqing-chengkou.jpg', 'ON_SALE', 980),

('江西赣南腊肉',
 '赣南客家腊肉，传承百年客家腌制技艺，选用赣南山地土猪肉，以粗盐、料酒、八角等腌制后炭火慢烘。肉质紧实有嚼劲，咸香适中，带有一丝炭烤的焦香风味，是客家人逢年过节的必备食材。',
 65.00, 85.00, 320, '腊肉', '/images/products/jiangxi-gannan.jpg', 'ON_SALE', 1430);
