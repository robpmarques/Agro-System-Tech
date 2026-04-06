-- =======================================
-- EXTENSIONS
-- =======================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =======================================
-- USERS
-- =======================================

CREATE TABLE users
(
    id         UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    role       VARCHAR(32)  NOT NULL CHECK (role IN ('ADMIN', 'OPERADOR', 'ESPECIALISTA')),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- =======================================
-- AREAS
-- =======================================

CREATE TABLE areas
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR,
    location   VARCHAR,
    size       DOUBLE PRECISION,
    user_id    UUID NOT NULL,
    created_at TIMESTAMP        DEFAULT NOW(),

    CONSTRAINT fk_areas_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

-- =======================================
-- SENSOR PLANS
-- =======================================

CREATE TABLE sensor_plans
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    area_id       UUID NOT NULL,
    requested_by  UUID NOT NULL,
    specialist_id UUID,

    status        VARCHAR,
    notes         TEXT,

    created_at    TIMESTAMP        DEFAULT NOW(),
    reviewed_at   TIMESTAMP,

    CONSTRAINT fk_plan_area
        FOREIGN KEY (area_id) REFERENCES areas (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_plan_requested_by
        FOREIGN KEY (requested_by) REFERENCES users (id),

    CONSTRAINT fk_plan_specialist
        FOREIGN KEY (specialist_id) REFERENCES users (id)
);

-- =======================================
-- PLANNED SENSORS
-- =======================================

CREATE TABLE planned_sensors
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    plan_id    UUID NOT NULL,

    name       VARCHAR,
    type       VARCHAR,
    position   VARCHAR,

    created_at TIMESTAMP        DEFAULT NOW(),

    CONSTRAINT fk_planned_sensor_plan
        FOREIGN KEY (plan_id) REFERENCES sensor_plans (id)
            ON DELETE CASCADE
);

-- =======================================
-- SENSORS
-- =======================================

CREATE TABLE sensors
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name       VARCHAR,
    type       VARCHAR,
    position   VARCHAR,

    area_id    UUID NOT NULL,
    is_active  BOOLEAN          DEFAULT TRUE,

    created_at TIMESTAMP        DEFAULT NOW(),

    CONSTRAINT fk_sensor_area
        FOREIGN KEY (area_id) REFERENCES areas (id)
            ON DELETE CASCADE
);

-- =======================================
-- SENSOR READINGS
-- =======================================

CREATE TABLE sensor_readings
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    sensor_id   UUID      NOT NULL,
    value       DOUBLE PRECISION,
    data        JSONB,
    recorded_at TIMESTAMP NOT NULL,
    created_at  TIMESTAMP        DEFAULT NOW(),

    CONSTRAINT fk_reading_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors (id)
            ON DELETE CASCADE
);

-- Índices importantes (time series)

CREATE INDEX idx_readings_sensor_time
    ON sensor_readings (sensor_id, recorded_at);

CREATE INDEX idx_readings_time
    ON sensor_readings (recorded_at);

-- =======================================
-- RULES
-- =======================================

CREATE TABLE rules
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    name       VARCHAR,
    operator   VARCHAR,
    threshold  DOUBLE PRECISION,
    is_active  BOOLEAN          DEFAULT TRUE,

    sensor_id  UUID NOT NULL,
    user_id    UUID NOT NULL,

    created_at TIMESTAMP        DEFAULT NOW(),

    CONSTRAINT fk_rule_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_rule_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

-- =======================================
-- ALERTS
-- =======================================

CREATE TABLE alerts
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    sensor_id    UUID             NOT NULL,
    rule_id      UUID             NOT NULL,

    value        DOUBLE PRECISION NOT NULL,

    message      VARCHAR,
    status       VARCHAR,

    triggered_at TIMESTAMP,
    resolved_at  TIMESTAMP,

    CONSTRAINT fk_alert_sensor
        FOREIGN KEY (sensor_id) REFERENCES sensors (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_alert_rule
        FOREIGN KEY (rule_id) REFERENCES rules (id)
);

-- Índice para buscar alertas ativos
CREATE INDEX idx_alerts_status
    ON alerts (status);

-- Índice para regras por sensor
CREATE INDEX idx_rules_sensor
    ON rules (sensor_id);