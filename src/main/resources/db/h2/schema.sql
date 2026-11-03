CREATE TABLE IF NOT EXISTS vets (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  first_name VARCHAR(30) NOT NULL,
  last_name VARCHAR(30) NOT NULL
);

CREATE INDEX idx_vets_last_name ON vets(last_name);

CREATE TABLE IF NOT EXISTS specialties (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(80) NOT NULL
);

CREATE INDEX idx_specialties_name ON specialties(name);

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE,
  FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE CASCADE,
  UNIQUE (vet_id, specialty_id)
);

CREATE TABLE IF NOT EXISTS types (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(80) NOT NULL
);

CREATE INDEX idx_types_name ON types(name);

CREATE TABLE IF NOT EXISTS owners (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  first_name VARCHAR(30) NOT NULL,
  last_name VARCHAR(30) NOT NULL,
  address VARCHAR(255) NOT NULL,
  city VARCHAR(80) NOT NULL,
  telephone VARCHAR(20) NOT NULL
);

CREATE INDEX idx_owners_last_name ON owners(last_name);

CREATE TABLE IF NOT EXISTS pets (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(30) NOT NULL,
  birth_date DATE NOT NULL,
  type_id INTEGER NOT NULL,
  owner_id INTEGER NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE,
  FOREIGN KEY (type_id) REFERENCES types(id) ON DELETE CASCADE
);

CREATE INDEX idx_pets_name ON pets(name);

CREATE TABLE IF NOT EXISTS visits (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  pet_id INTEGER NOT NULL,
  visit_date DATE NOT NULL,
  description VARCHAR(255) NOT NULL,
  FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(20) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS roles (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username VARCHAR(20) NOT NULL,
  role VARCHAR(20) NOT NULL,
  UNIQUE (role, username),
  FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- JIRA-402

CREATE TABLE IF NOT EXISTS owner_user_links (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_id INTEGER NOT NULL,
  username VARCHAR(20) NOT NULL,
  verified BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (username),
  UNIQUE (owner_id),
  FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE,
  FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vet_availability_slots (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  vet_id INTEGER NOT NULL,
  start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_vet_availability_slots_vet_id ON vet_availability_slots(vet_id);

CREATE TABLE IF NOT EXISTS video_consultations (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  owner_id INTEGER NOT NULL,
  vet_id INTEGER NOT NULL,
  start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  triage_level VARCHAR(16) NOT NULL,
  triage_priority VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL,
  reason VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE CASCADE,
  FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_video_consultations_vet_start_end
  ON video_consultations(vet_id, start_date_time, end_date_time);

CREATE INDEX IF NOT EXISTS idx_video_consultations_owner_id
  ON video_consultations(owner_id);
