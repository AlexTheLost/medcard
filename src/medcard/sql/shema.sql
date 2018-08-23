CREATE TABLE CARD
(id serial PRIMARY KEY,
 create_time timestamp DEFAULT now() NOT NULL,
 family_name text NOT NULL,
 first_name text NOT NULL,
 last_name text NOT NULL,
 gender varchar(10) NOT NULL,
 birthdate date NOT NULL,
 blood_type VARCHAR(2) NOT NULL,
 height INTEGER NOT NULL,
 weight INTEGER NOT NULL
);


CREATE TABLE RECORD
(id serial PRIMARY KEY,
 card_id integer REFERENCES CARD (id),
 create_time timestamp DEFAULT now() NOT NULL,
 name text NOT NULL,
 description text NOT NULL
);


CREATE TABLE USERS
(id serial PRIMARY KEY,
 login text NOT NULL,
 password text NOT NULL
);


CREATE TABLE RECORD_HISTORY
(id serial PRIMARY KEY,
 orign_id INTEGER,
 card_id integer REFERENCES CARD (id),
 create_time timestamp DEFAULT now() NOT NULL,
 name text NOT NULL,
 description text NOT NULL
);


CREATE TABLE CARD_HISTORY
(id serial PRIMARY KEY,
 orign_id INTEGER,
	update_time timestamp default now() not null,
	family_name text not null,
	first_name text not null,
	last_name text not null,
	gender varchar(10) not null,
	birthdate date not null,
	blood_type varchar(2) not null,
	height integer not null,
	weight integer not null
);


CREATE OR REPLACE FUNCTION save_log()
  RETURNS trigger AS
$BODY$
BEGIN
 INSERT INTO CARD_HISTORY(orign_id, family_name, first_name, last_name, gender, birthdate, blood_type, height, weight)
 VALUES(OLD.id, OLD.family_name, OLD.first_name, OLD.last_name, OLD.gender, OLD.birthdate, OLD.blood_type, OLD.height, OLD.weight);

 RETURN NEW;
END;
$BODY$

LANGUAGE plpgsql VOLATILE
COST 100;


CREATE TRIGGER card_changes
  AFTER UPDATE
  ON CARD
  FOR EACH ROW
  EXECUTE PROCEDURE save_log();
