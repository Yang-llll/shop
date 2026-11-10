USE braised_pork_db;
DELETE FROM order_item WHERE product_id IN (SELECT id FROM product);
DELETE FROM product;

INSERT INTO product (name, description, price, original_price, stock, category, image_url, status, sales_count) VALUES
('湖南湘西农家柴火腊肉', '湘西土家族传统柴火腊肉，精选本地散养黑猪五花肉，用柏树枝、茶壳、橘皮混合烟熏40天以上。肥瘦相间，层次分明，腊香醇厚，越蒸越香。切片后配蒜苗爆炒，是湘西人最地道的家常味道。每袋500g，真空包装锁鲜。', 78.00, 108.00, 200, '腊肉', '/images/products/xiangxi-chaihuo.svg', 'ON_SALE', 3260),

('四川成都麻辣香肠', '正宗川味麻辣香肠，选用新鲜猪后腿肉，手工切丁后配以汉源花椒、二荆条辣椒、白酒、冰糖等腌制灌入天然肠衣。经7天风干晾晒，麻辣鲜香，肥而不腻。可直接蒸熟切片，也可切段煎炒。每袋400g。', 52.00, 68.00, 350, '腊肠', '/images/products/sichuan-mala.svg', 'ON_SALE', 4180),

('云南宣威老火腿', '宣威火腿，中国三大名腿之一，选用乌金猪后腿，经传统工艺腌制后自然发酵24个月以上。色泽鲜艳，切面呈玫瑰红，肥肉透明，瘦肉紧实。可生食薄切，也可炖汤、炒菜、蒸食，风味独特。整腿约2kg。', 198.00, 268.00, 80, '火腿', '/images/products/xuanwei-ham.svg', 'ON_SALE', 1560),

('贵州遵义风干老腊肉', '遵义苗寨风干腊肉，取海拔1500米以上散养土猪肉，以粗盐、花椒、八角腌制后悬挂于火塘上方，经自然风干60余天。不烟熏、不烘烤，完全靠山间冷风自然干燥，肉质紧实，脂肪呈琥珀色透明状，口感醇厚回甘。每袋500g。', 86.00, 118.00, 150, '腊肉', '/images/products/zunyi-fenggan.svg', 'ON_SALE', 2080);
