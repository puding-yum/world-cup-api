-- Entity JPA memakai Long (BIGINT), sementara V1 memakai SERIAL/INTEGER.
-- Lebarkan semua kolom id dan foreign key ke BIGINT agar cocok dengan entity
-- (ddl-auto=validate). Sequence SERIAL tetap berfungsi setelah perubahan tipe.

ALTER TABLE teams
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE players
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN team_id TYPE BIGINT;

ALTER TABLE matches
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE match_teams
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN match_id TYPE BIGINT,
    ALTER COLUMN team_id TYPE BIGINT;

ALTER TABLE match_goals
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN player_id TYPE BIGINT,
    ALTER COLUMN match_team_id TYPE BIGINT;

ALTER TABLE match_penalties
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN player_id TYPE BIGINT,
    ALTER COLUMN match_team_id TYPE BIGINT;

ALTER TABLE match_cards
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN player_id TYPE BIGINT,
    ALTER COLUMN match_team_id TYPE BIGINT;
