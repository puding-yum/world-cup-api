-- World Cup Scoreboard - skema awal (sesuai ERD DBML di spesifikasi).
-- Catatan desain penting:
--   * skor TIDAK disimpan, diturunkan dari COUNT(match_goals) per match_team.
--   * event (goals/cards/penalties) tidak menyimpan match_id; match diturunkan
--     lewat match_team_id -> match_teams -> matches.
--   * hapus match harus cascade ke match_teams lalu ke semua event di bawahnya.
--   * semua waktu disimpan UTC (TIMESTAMPTZ).

CREATE TABLE teams (
    id                SERIAL PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    region_match_code VARCHAR(10)  NOT NULL, -- kode display bracket, mis. "ARG"
    region_flag_code  VARCHAR(10)  NOT NULL, -- ISO 3166-1 alpha-2 utk flagcdn.com
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE players (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    team_id       INTEGER      NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    jersey_number INTEGER,
    position      VARCHAR(30),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_players_team_id ON players (team_id);

CREATE TABLE matches (
    id               SERIAL PRIMARY KEY,
    match_code       VARCHAR(30),
    bracket_position VARCHAR(20),
    match_date       TIMESTAMPTZ,
    match_status     VARCHAR(20)  NOT NULL DEFAULT 'upcoming'
        CONSTRAINT chk_match_status CHECK (match_status IN ('upcoming', 'ongoing', 'ended')),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_matches_status ON matches (match_status);
CREATE INDEX idx_matches_match_date ON matches (match_date);

CREATE TABLE match_teams (
    id         SERIAL PRIMARY KEY,
    match_id   INTEGER     NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    team_id    INTEGER     NOT NULL REFERENCES teams (id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_match_team UNIQUE (match_id, team_id)
);
CREATE INDEX idx_match_teams_match_id ON match_teams (match_id);
CREATE INDEX idx_match_teams_team_id ON match_teams (team_id);

CREATE TABLE match_goals (
    id            SERIAL PRIMARY KEY,
    player_id     INTEGER     REFERENCES players (id) ON DELETE SET NULL,
    match_team_id INTEGER     NOT NULL REFERENCES match_teams (id) ON DELETE CASCADE, -- tim yang diuntungkan
    minute        INTEGER,
    second        INTEGER     CONSTRAINT chk_goal_second CHECK (second BETWEEN 0 AND 59),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_match_goals_match_team_id ON match_goals (match_team_id);

CREATE TABLE match_penalties (
    id            SERIAL PRIMARY KEY,
    player_id     INTEGER     REFERENCES players (id) ON DELETE SET NULL,
    match_team_id INTEGER     NOT NULL REFERENCES match_teams (id) ON DELETE CASCADE, -- tim yang menendang
    kick_order    INTEGER     NOT NULL,
    is_scored     BOOLEAN     NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_penalty_kick_order UNIQUE (match_team_id, kick_order)
);
CREATE INDEX idx_match_penalties_match_team_id ON match_penalties (match_team_id);

CREATE TABLE match_cards (
    id            SERIAL PRIMARY KEY,
    player_id     INTEGER     NOT NULL REFERENCES players (id) ON DELETE CASCADE,
    match_team_id INTEGER     NOT NULL REFERENCES match_teams (id) ON DELETE CASCADE, -- tim penerima kartu
    card_type     VARCHAR(20) NOT NULL
        CONSTRAINT chk_card_type CHECK (card_type IN ('yellow', 'red', 'second_yellow_red')),
    minute        INTEGER,
    second        INTEGER     CONSTRAINT chk_card_second CHECK (second BETWEEN 0 AND 59),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_match_cards_match_team_id ON match_cards (match_team_id);
