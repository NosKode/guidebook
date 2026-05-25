-- Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Enums
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE place_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Users
CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100),
    role          user_role    NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Categories
CREATE TABLE categories (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- Places
CREATE TABLE places (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255)  NOT NULL,
    address     VARCHAR(500),
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    category_id INT           REFERENCES categories(id),
    description TEXT,
    cover_path  VARCHAR(500),
    uploaded_by UUID          REFERENCES users(id),
    status      place_status  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Photos
CREATE TABLE photos (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    place_id   UUID         NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    file_path  VARCHAR(500) NOT NULL,
    caption    VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Reviews
CREATE TABLE reviews (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    place_id   UUID      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id),
    rating     INT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (place_id, user_id)
);

-- Favorites
CREATE TABLE favorites (
    user_id    UUID      NOT NULL REFERENCES users(id),
    place_id   UUID      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    added_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, place_id)
);

-- Indexes
CREATE INDEX idx_places_status      ON places(status);
CREATE INDEX idx_places_category_id ON places(category_id);
CREATE INDEX idx_reviews_place_id   ON reviews(place_id);
CREATE INDEX idx_favorites_user_id  ON favorites(user_id);
