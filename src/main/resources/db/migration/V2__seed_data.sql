INSERT INTO users (id, email, name) VALUES (1, 'john@example.com', 'John');
INSERT INTO users (id, email, name) VALUES (2, 'jane@example.com', 'Jane');
INSERT INTO users (id, email, name) VALUES (3, 'john2@example.com', 'John');

INSERT INTO users_roles (user_id, roles) VALUES (1, 'user');
INSERT INTO users_roles (user_id, roles) VALUES (2, 'user');
INSERT INTO users_roles (user_id, roles) VALUES (3, 'admin');
INSERT INTO users_roles (user_id, roles) VALUES (3, 'user');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
