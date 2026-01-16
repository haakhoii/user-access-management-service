CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
