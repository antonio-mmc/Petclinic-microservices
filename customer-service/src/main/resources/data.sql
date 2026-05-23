-- PetTypes
INSERT INTO pet_types (name) VALUES ('Dog');
INSERT INTO pet_types (name) VALUES ('Cat');

-- Owners
INSERT INTO owners (first_name, last_name, address, city, telephone)
VALUES ('George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');

INSERT INTO owners (first_name, last_name, address, city, telephone)
VALUES ('Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');

-- Pets
-- George's dog (ACTIVE)
INSERT INTO pets (name, birth_date, status, type_id, owner_id)
VALUES ('Leo', '2020-09-07', 'ACTIVE', 1, 1);

-- Betty's cat (ACTIVE)
INSERT INTO pets (name, birth_date, status, type_id, owner_id)
VALUES ('Bastet', '2019-03-15', 'ACTIVE', 2, 2);

-- Betty's second cat (INACTIVE — retired)
INSERT INTO pets (name, birth_date, status, type_id, owner_id)
VALUES ('Whiskers', '2015-06-20', 'INACTIVE', 2, 2);
