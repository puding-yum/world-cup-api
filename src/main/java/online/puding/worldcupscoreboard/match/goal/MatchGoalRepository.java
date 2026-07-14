package online.puding.worldcupscoreboard.match.goal;

import online.puding.worldcupscoreboard.match.MatchTeamCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MatchGoalRepository extends JpaRepository<MatchGoal, Long> {

    List<MatchGoal> findByMatchTeamIdInOrderByMinuteAscSecondAsc(Collection<Long> matchTeamIds);

    /** Skor tiap tim = jumlah gol per match_team_id, dihitung sekaligus (anti-N+1). */
    @Query("""
            select g.matchTeamId as matchTeamId, count(g) as total
            from MatchGoal g
            where g.matchTeamId in :matchTeamIds
            group by g.matchTeamId
            """)
    List<MatchTeamCount> countByMatchTeamIds(@Param("matchTeamIds") Collection<Long> matchTeamIds);
}
