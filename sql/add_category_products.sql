USE braised_pork_db;

-- 旧数据的版本号为 NULL 时，Hibernate 在下单扣减库存时会触发 longValue 异常。
UPDATE product SET version = 0 WHERE version IS NULL;
ALTER TABLE product MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;

-- 修正现有商品的分类标签，使商品描述与筛选结果一致。
UPDATE product
SET name = '柏枝熏腊排骨 500g',
    category = '腊排骨,柏枝熏制,柏香味'
WHERE name IN ('腊排骨 500g', '柏枝熏腊排骨 500g');

UPDATE product
SET name = '花椒味土家腊肠 500g',
    description = '精选猪肉手工灌制，以花椒提香，麻香醇厚而不辛辣，柴火轻熏后肉香饱满。',
    category = '腊肠,柴火熏制,花椒味',
    image_url = '/img/peppercorn-tujia-sausage.webp',
    images = '["/img/peppercorn-tujia-sausage.webp"]'
WHERE name IN ('土家腊肠 500g', '花椒味土家腊肠 500g');

UPDATE product
SET category = '后腿腊肉,柏枝熏制,柏香味',
    image_url = '/img/cypress-hind-leg.webp',
    images = '["/img/cypress-hind-leg.webp"]'
WHERE name = '柏枝熏后腿 500g';

UPDATE product
SET name = '原味柴火腊猪头 500g',
    description = '精选猪头肉，以粗盐原味腌制后柴火慢熏，皮糯肉香，适合蒸制或切片佐餐。',
    category = '腊猪头,原味腌制,柴火熏制'
WHERE name IN ('腊猪头 500g', '原味柴火腊猪头 500g');

UPDATE product
SET name = '原味柴火腊猪脚 500g',
    description = '精选带筋猪脚，以粗盐原味腌制后柴火慢熏，胶质丰富、腊香醇厚，适合炖煮。',
    category = '腊猪脚,原味腌制,柴火熏制',
    image_url = '/img/original-firewood-trotter.webp',
    images = '["/img/original-firewood-trotter.webp"]'
WHERE name IN ('腊猪脚 500g', '原味柴火腊猪脚 500g');

UPDATE product
SET name = '辣味柴火五花腊肉 500g',
    description = '精选五花肉，以干辣椒腌制后柴火慢熏，肥瘦层次分明，辣香浓郁。',
    category = '五花腊肉,柴火熏制,辣椒味',
    image_url = '/img/spicy-firewood-pork-belly.webp',
    images = '["/img/spicy-firewood-pork-belly.webp"]'
WHERE name IN ('柴火五花腊肉 500g', '辣味柴火五花腊肉 500g');

-- 补充茶壳、果木和特色口味商品；重复执行不会重复插入。
INSERT INTO product
    (name, description, price, original_price, stock, category, image_url, images, status, sales_count, version)
SELECT
    '茶壳熏辣味腊排骨 500g',
    '精选猪肋排，以油茶壳低温慢熏，加入干辣椒腌制，茶香清雅、辣味鲜明，适合蒸制或炖煮。',
    76.00, 96.00, 75, '腊排骨,茶壳熏制,辣椒味',
    '/img/tea-shell-spicy-ribs.webp', '["/img/tea-shell-spicy-ribs.webp"]',
    'ON_SALE', 18, 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '茶壳熏辣味腊排骨 500g');

INSERT INTO product
    (name, description, price, original_price, stock, category, image_url, images, status, sales_count, version)
SELECT
    '果木熏广式腊肠 500g',
    '精选猪后腿肉，配以白酒和冰糖调味，再用果木轻熏，酒香微甜、肉香饱满，适合蒸饭或切片食用。',
    66.00, 86.00, 100, '腊肠,果木熏制,广式甜味',
    '/img/fruitwood-cantonese-sausage.webp', '["/img/fruitwood-cantonese-sausage.webp"]',
    'ON_SALE', 22, 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '果木熏广式腊肠 500g');

INSERT INTO product
    (name, description, price, original_price, stock, category, image_url, images, status, sales_count, version)
SELECT
    '柏香腊猪脚 500g',
    '精选带筋猪脚，以柏枝慢火熏制，表皮油亮、胶质丰富，带有自然柏木清香，适合炖汤或红烧。',
    59.00, 79.00, 65, '腊猪脚,柏枝熏制,柏香味',
    '/img/cypress-smoked-trotter.webp', '["/img/cypress-smoked-trotter.webp"]',
    'ON_SALE', 16, 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '柏香腊猪脚 500g');

INSERT INTO product
    (name, description, price, original_price, stock, category, image_url, images, status, sales_count, version)
SELECT
    '酱香腊猪头肉 500g',
    '精选腊猪头肉，以豆瓣酱和香辛料充分腌制后柴火慢熏，皮糯肉香、酱香浓郁，切片即可佐餐。',
    52.00, 69.00, 80, '腊猪头,柴火熏制,酱香味',
    '/img/sauce-cured-pork-head.webp', '["/img/sauce-cured-pork-head.webp"]',
    'ON_SALE', 19, 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '酱香腊猪头肉 500g');

-- 统一使用能够直接展示熏制过程、对应燃料和口味特征的商品图片。
UPDATE product SET image_url = '/img/firewood-original-pork-belly-smoking.webp', images = '["/img/firewood-original-pork-belly-smoking.webp"]'
WHERE name = '五花腊肉 500g';

UPDATE product SET image_url = '/img/firewood-original-hind-leg-smoking.webp', images = '["/img/firewood-original-hind-leg-smoking.webp"]'
WHERE name = '后腿腊肉 500g';

UPDATE product SET image_url = '/img/cypress-ribs-smoking.webp', images = '["/img/cypress-ribs-smoking.webp"]'
WHERE name = '柏枝熏腊排骨 500g';

UPDATE product SET image_url = '/img/firewood-original-pork-head-smoking.webp', images = '["/img/firewood-original-pork-head-smoking.webp"]'
WHERE name = '原味柴火腊猪头 500g';

UPDATE product SET image_url = '/img/firewood-original-trotter-smoking.webp', images = '["/img/firewood-original-trotter-smoking.webp"]'
WHERE name = '原味柴火腊猪脚 500g';

UPDATE product SET image_url = '/img/firewood-peppercorn-sausage-smoking.webp', images = '["/img/firewood-peppercorn-sausage-smoking.webp"]'
WHERE name = '花椒味土家腊肠 500g';

UPDATE product
SET description = '糯米血粑与腊肉一同以柴火慢熏，米香、肉香与烟熏香融合，是湘西传统腊味。',
    category = '其他腊制品,柴火熏制,原味腌制',
    image_url = '/img/firewood-blood-cake-smoking.webp',
    images = '["/img/firewood-blood-cake-smoking.webp"]'
WHERE name = '血粑腊肉';

UPDATE product
SET description = '精选猪耳以粗盐腌制后柴火慢熏，口感脆爽、腊香自然，适合切丝凉拌。',
    category = '其他腊制品,柴火熏制,原味腌制',
    image_url = '/img/firewood-pig-ear-smoking.webp',
    images = '["/img/firewood-pig-ear-smoking.webp"]'
WHERE name = '腊猪耳';

UPDATE product SET image_url = '/img/firewood-spicy-pork-belly-smoking.webp', images = '["/img/firewood-spicy-pork-belly-smoking.webp"]'
WHERE name = '辣味柴火五花腊肉 500g';

UPDATE product SET image_url = '/img/cypress-hind-leg-smoking.webp', images = '["/img/cypress-hind-leg-smoking.webp"]'
WHERE name = '柏枝熏后腿 500g';

UPDATE product SET image_url = '/img/tea-shell-spicy-ribs-smoking.webp', images = '["/img/tea-shell-spicy-ribs-smoking.webp"]'
WHERE name = '茶壳熏辣味腊排骨 500g';

UPDATE product SET image_url = '/img/fruitwood-sweet-sausage-smoking.webp', images = '["/img/fruitwood-sweet-sausage-smoking.webp"]'
WHERE name = '果木熏广式腊肠 500g';

UPDATE product SET image_url = '/img/cypress-trotter-smoking.webp', images = '["/img/cypress-trotter-smoking.webp"]'
WHERE name = '柏香腊猪脚 500g';

UPDATE product SET image_url = '/img/firewood-sauce-pork-head-smoking.webp', images = '["/img/firewood-sauce-pork-head-smoking.webp"]'
WHERE name = '酱香腊猪头肉 500g';
