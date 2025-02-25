DROP TABLE IF EXISTS word;
CREATE TABLE word
(
    "id" bigserial primary key,
    "name" varchar(255),
    "first_letter" varchar(255)
);