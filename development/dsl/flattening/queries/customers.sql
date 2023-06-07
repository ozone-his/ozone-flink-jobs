SELECT `customer_id` AS `customer_id`,
       concat(`contact_title`,'  ', `contact_name`) AS `full_name`
FROM `customers`