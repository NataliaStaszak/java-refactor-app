CREATE TABLE users (
    email VARCHAR(255) PRIMARY KEY,
    name  VARCHAR(255) NOT NULL
);

CREATE TABLE users_roles (
    user_email VARCHAR(255) NOT NULL REFERENCES users(email) ON DELETE CASCADE,
    roles      VARCHAR(255) NOT NULL
);
