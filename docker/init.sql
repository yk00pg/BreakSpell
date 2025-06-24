-- 注意：
-- Windowsで実行する場合は文字化け防止のために、構文の末尾に 'DEFAULT CHARSET=utf8' を指定してください。
-- 例）CREATE TABLE player_score (...) DEFAULT CHARSET=utf8;

SET GLOBAL authentication_policy = 'caching_sha2_password';

CREATE DATABASE IF NOT EXISTS break_spell;
USE break_spell;

CREATE TABLE player_score(
  id INT AUTO_INCREMENT,
  player_uuid VARCHAR(36),
  player_name VARCHAR(16),
  score INT,
  difficulty VARCHAR(6),
  registered_at DATETIME,
  PRIMARY KEY (id));

CREATE TABLE player_progress(
  player_uuid VARCHAR(36),
  difficulty VARCHAR(6),
  played BOOLEAN,
  cleared BOOLEAN,
  cleared_at DATETIME,
  PRIMARY KEY (player_uuid, difficulty));