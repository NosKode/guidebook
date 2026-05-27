-- V5: Seed admin user (password: Admin1234)
-- pgcrypto bf = bcrypt, compatible with jBCrypt checkpw
INSERT INTO users (email, password_hash, display_name, role)
SELECT
    'admin@guidebook.com',
    crypt('Admin1234', gen_salt('bf', 12)),
    'Администратор',
    'ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@guidebook.com'
);
