DROP TABLE video_consultations IF EXISTS;
DROP TABLE vet_availability_slots IF EXISTS;
DROP TABLE owner_user_links IF EXISTS;
DROP TABLE vet_specialties IF EXISTS;
DROP TABLE vets IF EXISTS;
DROP TABLE specialties IF EXISTS;
DROP TABLE visits IF EXISTS;
DROP TABLE pets IF EXISTS;
DROP TABLE types IF EXISTS;
DROP TABLE owners IF EXISTS;
DROP TABLE roles IF EXISTS;
DROP TABLE users IF EXISTS;


CREATE TABLE vets (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR(30)
);
CREATE INDEX vets_last_name ON vets (last_name);

CREATE TABLE specialties (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties (
  vet_id       INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL
);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);

CREATE TABLE types (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX types_name ON types (name);

CREATE TABLE owners (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR_IGNORECASE(30),
  address    VARCHAR(255),
  city       VARCHAR(80),
  telephone  VARCHAR(20)
);
CREATE INDEX owners_last_name ON owners (last_name);

CREATE TABLE pets (
  id         INTEGER IDENTITY PRIMARY KEY,
  name       VARCHAR(30),
  birth_date DATE,
  type_id    INTEGER NOT NULL,
  owner_id   INTEGER NOT NULL
);
ALTER TABLE pets ADD CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE pets ADD CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types (id);
CREATE INDEX pets_name ON pets (name);

CREATE TABLE visits (
  id          INTEGER IDENTITY PRIMARY KEY,
  pet_id      INTEGER NOT NULL,
  visit_date  DATE,
  description VARCHAR(255)
);
ALTER TABLE visits ADD CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets (id);
CREATE INDEX visits_pet_id ON visits (pet_id);

CREATE  TABLE users (
  username    VARCHAR(20) NOT NULL ,
  password    VARCHAR(60) NOT NULL ,
  enabled     BOOLEAN DEFAULT TRUE NOT NULL ,
  PRIMARY KEY (username)
);

CREATE TABLE roles (
  id              INTEGER IDENTITY PRIMARY KEY,
  username        VARCHAR(20) NOT NULL,
  role            VARCHAR(20) NOT NULL
);
ALTER TABLE roles ADD CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES users (username);
CREATE INDEX fk_username_idx ON roles (username);

CREATE TABLE owner_user_links (
  id        INTEGER IDENTITY PRIMARY KEY,
  owner_id  INTEGER NOT NULL,
  username  VARCHAR(20) NOT NULL,
  verified  BOOLEAN DEFAULT TRUE NOT NULL
);
ALTER TABLE owner_user_links ADD CONSTRAINT fk_owner_user_links_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE owner_user_links ADD CONSTRAINT fk_owner_user_links_users FOREIGN KEY (username) REFERENCES users (username);
ALTER TABLE owner_user_links ADD CONSTRAINT uq_owner_user_links_username UNIQUE (username);
ALTER TABLE owner_user_links ADD CONSTRAINT uq_owner_user_links_owner_id UNIQUE (owner_id);

CREATE TABLE vet_availability_slots (
  id              INTEGER IDENTITY PRIMARY KEY,
  vet_id          INTEGER NOT NULL,
  start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date_time   TIMESTAMP WITH TIME ZONE NOT NULL
);
ALTER TABLE vet_availability_slots ADD CONSTRAINT fk_vet_availability_slots_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
CREATE INDEX vet_availability_slots_vet_id ON vet_availability_slots (vet_id);

CREATE TABLE video_consultations (
  id              INTEGER IDENTITY PRIMARY KEY,
  owner_id        INTEGER NOT NULL,
  vet_id          INTEGER NOT NULL,
  start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date_time   TIMESTAMP WITH TIME ZONE NOT NULL,
  triage_level    VARCHAR(16) NOT NULL,
  triage_priority VARCHAR(16) NOT NULL,
  status          VARCHAR(16) NOT NULL,
  reason          VARCHAR(500) NOT NULL,
  created_at      TIMESTAMP NOT NULL
);
ALTER TABLE video_consultations ADD CONSTRAINT fk_video_consultations_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE video_consultations ADD CONSTRAINT fk_video_consultations_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
CREATE INDEX video_consultations_owner_id ON video_consultations (owner_id);
CREATE INDEX video_consultations_vet_id ON video_consultations (vet_id);

