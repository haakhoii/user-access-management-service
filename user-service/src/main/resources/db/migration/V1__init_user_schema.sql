CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,

    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    avatar_url TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profile_roles (
    profile_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_profile_roles_profile
        FOREIGN KEY (profile_id)
        REFERENCES user_profiles(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_profile_role
        UNIQUE (profile_id, role)
);

