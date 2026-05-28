-- V7: Fix GPS coordinates (50–1000 m errors in V6)

UPDATE places SET latitude = 55.8223, longitude = 37.6458 WHERE name = 'Гостиница «Космос»';
UPDATE places SET latitude = 55.8342, longitude = 37.6211 WHERE name = 'Москвариум';
UPDATE places SET latitude = 55.7902, longitude = 37.5313 WHERE name = 'Авиапарк';
UPDATE places SET latitude = 55.9667, longitude = 37.4157 WHERE name = 'Аэропорт Шереметьево (SVO)';
UPDATE places SET latitude = 55.4103, longitude = 37.9025 WHERE name = 'Аэропорт Домодедово (DME)';
UPDATE places SET latitude = 55.7743, longitude = 37.6540 WHERE name = 'Казанский вокзал';
UPDATE places SET latitude = 55.7306, longitude = 37.5041 WHERE name = 'Музей Победы';
UPDATE places SET latitude = 55.8046, longitude = 37.6706 WHERE name = 'Парк Сокольники';
UPDATE places SET latitude = 55.7214, longitude = 37.5900 WHERE name = 'Нескучный сад';
UPDATE places SET latitude = 55.7302, longitude = 37.6033 WHERE name = 'Парк Горького';
