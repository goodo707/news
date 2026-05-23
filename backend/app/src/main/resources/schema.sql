CREATE TABLE IF NOT EXISTS article (
    article_id TEXT NOT NULL PRIMARY KEY,
    title      TEXT NOT NULL,
    link       TEXT NOT NULL,
    author     TEXT,
    category   TEXT NOT NULL,
    pub_date   TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS users (
    id        INTEGER PRIMARY KEY,
    name      TEXT NOT NULL,
    device_id TEXT NOT NULL UNIQUE,
    push_type TEXT NOT NULL,
    dnd_start TEXT,
    dnd_end   TEXT
);

CREATE TABLE IF NOT EXISTS user_category (
    user_id  INTEGER NOT NULL,
    category TEXT    NOT NULL,
    PRIMARY KEY (user_id, category)
);

CREATE TABLE IF NOT EXISTS article_read (
    article_id TEXT NOT NULL PRIMARY KEY,
    read_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS push_log (
    id         INTEGER PRIMARY KEY,
    device_id  TEXT NOT NULL,
    push_type  TEXT NOT NULL,
    article_id TEXT NOT NULL,
    title      TEXT NOT NULL,
    category   TEXT NOT NULL,
    sent_at    TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    status     TEXT NOT NULL
);
