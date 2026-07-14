package online.puding.worldcupscoreboard.match.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Gol. {@code matchTeamId} = tim yang DIUNTUNGKAN (own-goal otomatis benar).
 * Tidak menyimpan match_id; match diturunkan lewat match_team_id.
 */
@Entity
@Table(name = "match_goals")
@Getter
@Setter
@NoArgsConstructor
public class MatchGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "match_team_id", nullable = false)
    private Long matchTeamId;

    @Column(name = "minute")
    private Integer minute;

    @Column(name = "second")
    private Integer second;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
