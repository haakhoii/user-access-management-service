ALTER TABLE users
ADD COLUMN role_id INT;

UPDATE users u
SET role_id = ur.role_id
FROM user_roles ur
WHERE u.id = ur.user_id;

UPDATE users
SET role_id = (
    SELECT id FROM roles WHERE name = 'ROLE_USER'
)
WHERE role_id IS NULL;

ALTER TABLE users
ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE users
ADD CONSTRAINT fk_users_role
FOREIGN KEY (role_id)
REFERENCES roles(id)
ON DELETE RESTRICT;

DROP TABLE user_roles;