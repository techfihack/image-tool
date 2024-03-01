use pip;

delete from image_labels where 1 = 1;

delete from questions where 1 = 1;

delete from labels where 1 = 1;

delete from images where 1 = 1;

-- generate random reserved labels
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'yellow');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'angry');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'computer');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'table');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'water');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'burger');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'dog');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'bird');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'phone');
INSERT INTO labels (id, label_name) VALUES (REPLACE(UUID(),'-',''), 'two');