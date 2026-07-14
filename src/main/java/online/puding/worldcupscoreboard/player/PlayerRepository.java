package online.puding.worldcupscoreboard.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamIdOrderByJerseyNumberAsc(Long teamId);

    /** Batch: ambil semua pemain untuk sekumpulan tim sekaligus (hindari N+1). */
    List<Player> findByTeamIdInOrderByTeamIdAscJerseyNumberAsc(Collection<Long> teamIds);
}
