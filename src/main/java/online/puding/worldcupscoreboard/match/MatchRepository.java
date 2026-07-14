package online.puding.worldcupscoreboard.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByMatchStatusOrderByMatchDateAsc(MatchStatus status);

    List<Match> findByMatchStatusOrderByMatchDateDesc(MatchStatus status);

    List<Match> findAllByOrderByMatchDateDesc();

    /** Match terdekat berikutnya yang belum dimulai. */
    Optional<Match> findFirstByMatchStatusOrderByMatchDateAsc(MatchStatus status);

    /** Match terakhir yang baru selesai. */
    Optional<Match> findFirstByMatchStatusOrderByMatchDateDesc(MatchStatus status);
}
