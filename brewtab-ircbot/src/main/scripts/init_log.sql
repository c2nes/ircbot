
-- Initialize database

CREATE TABLE sessions (
       id SERIAL PRIMARY KEY,
       start_time TIMESTAMP,
       end_time TIMESTAMP,
       active BOOLEAN
       );

CREATE TABLE events (
       id SERIAL PRIMARY KEY,
       event_time TIMESTAMP,
       type CHAR(16),
       nick VARCHAR(64),
       extra VARCHAR(256)
       );

CREATE TABLE messages (
       id SERIAL PRIMARY KEY,
       msg_time TIMESTAMP,
       channel VARCHAR(64),
       nick VARCHAR(64),
       message TEXT
       );

CREATE TABLE properties (
       k VARCHAR PRIMARY KEY,
       v bytea
       );

CREATE INDEX messages_nick_channel_idx ON messages(nick, channel);
CREATE INDEX messages_message_ts ON messages USING gin(to_tsvector('english', message));

CREATE INDEX properties_idx ON properties(k);
