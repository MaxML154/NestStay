-- NestStay-Shell demo seed (optional extras after migrations)
-- Ensures platform_view has enough rows for list/search UI.

USE `neststay_demo`;

-- Mark a few listings as selected for home page strips
UPDATE `platform_view` SET `is_selected` = 0;
UPDATE `platform_view` pv
INNER JOIN (
  SELECT MIN(id) AS id FROM `platform_view` GROUP BY `merchant_account` LIMIT 5
) pick ON pick.id = pv.id
SET pv.`is_selected` = 1;

-- Readable labels for placeholder homestay_info rows
UPDATE `homestay_info`
SET `rating` = '4.8',
    `service_tag` = '免费WiFi,近景区,交通便利,厨房,洗衣机'
WHERE `rating` LIKE '评分%' OR `service_tag` LIKE '服务%';
