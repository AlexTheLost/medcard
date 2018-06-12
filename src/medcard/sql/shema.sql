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
