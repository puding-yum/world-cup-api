package online.puding.worldcupscoreboard.match.card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MatchCardRepository extends JpaRepository<MatchCard, Long> {

    List<MatchCard> findByMatchTeamIdInOrderByMinuteAscSecondAsc(Collection<Long> matchTeamIds);
}
