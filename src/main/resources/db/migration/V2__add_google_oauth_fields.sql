ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;
ALTER TABLE users DROP COLUMN email_verified;
CREATE INDEX idx_users_google_id ON users(google_id);
