CREATE TABLE users(
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    email VARCHAR(50) UNIQUE,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(25) NOT NULL CHECK (role IN ('GUILDMASTER', 'ADVENTURER'))
);

CREATE TABLE adventurers(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE,
    class VARCHAR(25) NOT NULL CHECK (class IN ('WARRIOR', 'MAGE', 'ROGUE', 'PRIEST')),
    level INT DEFAULT 1,
    xp_point INT DEFAULT 0,
    recruitment_date TIMESTAMP DEFAULT NOW(),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);