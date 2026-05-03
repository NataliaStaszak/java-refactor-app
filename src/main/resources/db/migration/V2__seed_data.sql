INSERT INTO users (email, name) VALUES ('john@example.com', 'John');
INSERT INTO users (email, name) VALUES ('jane@example.com', 'Jane');
INSERT INTO users (email, name) VALUES ('john2@example.com', 'John');

INSERT INTO users_roles (user_email, roles) VALUES ('john@example.com', 'user');
INSERT INTO users_roles (user_email, roles) VALUES ('jane@example.com', 'user');
INSERT INTO users_roles (user_email, roles) VALUES ('john2@example.com', 'admin');
INSERT INTO users_roles (user_email, roles) VALUES ('john2@example.com', 'user');
