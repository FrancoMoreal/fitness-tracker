INSERT INTO users (username, password, email, enable, role, external_id)
VALUES
    ('admin', 'adminpass', 'admin@example.com', TRUE, 'ADMIN', 'uuid-1'),
    ('trainer1', 'trainerpass', 'trainer1@example.com', TRUE, 'TRAINER', 'uuid-2'),
    ('member1', 'memberpass', 'member1@example.com', TRUE, 'MEMBER', 'uuid-3');

INSERT INTO trainers (name, email, specialty, user_id)
VALUES
    ('Juan Pérez', 'trainer1@example.com', 'Fuerza', 2);

INSERT INTO members (name, email, age, weight, height, user_id, trainer_id)
VALUES
    ('Ana Gómez', 'member1@example.com', 28, 65.5, 1.68, 3, 1);
