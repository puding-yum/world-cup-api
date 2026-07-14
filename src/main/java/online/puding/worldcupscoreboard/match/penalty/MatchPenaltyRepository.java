package online.puding.worldcupscoreboard.match.penalty;

import online.puding.worldcupscoreboard.match.MatchTeamCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MatchPenaltyRepository extends JpaRepository<MatchPenalty, Long> {

    List<MatchPenalty> findByMatchTeamIdInOrderByKickOrderAsc(Collection<Long> matchTeamIds);

    boolean existsByMatchTeamIdAndKickOrder(Long matchTeamId, Integer kickOrder);

    /** Skor adu penalti = jumlah tendangan yang is_scored=true, per match_team_id. */
    @Query("""
            select p.matchTeamId as matchTeamId, count(p) as total
            from MatchPenalty p
            where p.scored = true and p.matchTeamId in :matchTeamIds
            group by p.matchTeamId
            """)
    List<MatchTeamCount> countScoredByMatchTeamIds(@Param("matchTeamIds") Collection<Long> matchTeamIds);

    /**
     * Jumlah SEMUA tendangan (gol maupun gagal) per match_team_id. Dipakai untuk
     * tahu tim mana yang ikut adu penalti — tim yang menendang tapi gagal semua
     * tidak punya baris di {@link #countScoredByMatchTeamIds}, jadi tanpa query ini
     * dia tidak bisa dibedakan dari tim yang memang tidak adu penalti.
     */
    @Query("""
            select p.matchTeamId as matchTeamId, count(p) as total
            from MatchPenalty p
            where p.matchTeamId in :matchTeamIds
            group by p.matchTeamId
            """)
    List<MatchTeamCount> countKicksByMatchTeamIds(@Param("matchTeamIds") Collection<Long> matchTeamIds);
}
