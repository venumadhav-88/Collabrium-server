-- Demo Accounts for Collabrium
-- This script creates test accounts for demo purposes

-- Demo Admin Account
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES (
  'Demo Admin',
  'admin@collabrium.edu',
  '$2a$10$91n6DUTJvBnRPxsOcZlmLOGvIJZk9YV1C5c1h0.xJhFt7N7OQH5B2',  -- bcrypt hash of 'Admin@1234'
  'ADMIN',
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Demo Researcher Account
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES (
  'Demo Researcher',
  'researcher@collabrium.edu',
  '$2a$10$G3QLVCKcSvVKnLxLyLaV..LyKKLDPfKv/OmClqZ2LCOwBXvJvfaCq',  -- bcrypt hash of 'Researcher@1234'
  'RESEARCHER',
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE updated_at = NOW();
