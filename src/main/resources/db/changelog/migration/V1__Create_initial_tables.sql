-- Crear tabla users (ajustado al nombre de la entidad)
CREATE TABLE IF NOT EXISTS users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       enable BOOLEAN NOT NULL DEFAULT TRUE,
                       role VARCHAR(20) NOT NULL,
                       external_id VARCHAR(36),
                       deleted_at TIMESTAMP NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Crear tabla Trainer
CREATE TABLE IF NOT EXISTS trainers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         specialty VARCHAR(255),
                         user_id BIGINT UNIQUE,
                         FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Crear tabla Member
CREATE TABLE IF NOT EXISTS members (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        age INT,
                        weight DOUBLE,
                        height DOUBLE,
                        user_id BIGINT UNIQUE,
                        trainer_id BIGINT,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (trainer_id) REFERENCES trainers(id)
);

-- Índices sugeridos para rendimiento
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_external_id ON users(external_id);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_enable ON users(enable);
