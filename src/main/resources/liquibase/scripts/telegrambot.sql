--liquibase formatted sql

-- changeSet asobolev:1
CREATE TABLE notification_task
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGSERIAL NOT NULL,
    message TEXT      NOT NULL,
    date    DATE      NOT NULL
);