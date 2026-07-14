package online.puding.worldcupscoreboard.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

    List<MatchTeam> findByMatchIdOrderByIdAsc(Long matchId);

    /** Batch: semua match_teams untuk sekumpulan match sekaligus (hindari N+1). */
    List<MatchTeam> findByMatchIdInOrderByIdAsc(Collection<Long> matchIds);
}
