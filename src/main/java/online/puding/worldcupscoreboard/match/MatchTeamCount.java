package online.puding.worldcupscoreboard.match;

/** Projeksi hasil agregasi COUNT per match_team_id (skor / jumlah penalti gol). */
public interface MatchTeamCount {

    Long getMatchTeamId();

    long getTotal();
}
