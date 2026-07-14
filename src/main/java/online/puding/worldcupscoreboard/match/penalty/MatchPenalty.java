package online.puding.worldcupscoreboard.match.penalty;

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

/** Tendangan adu penalti. {@code matchTeamId} = tim yang menendang. */
@Entity
@Table(name = "match_penalties")
@Getter
@Setter
@NoArgsConstructor
public class MatchPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "match_team_id", nullable = false)
    private Long matchTeamId;

    @Column(name = "kick_order", nullable = false)
    private Integer kickOrder;

    @Column(name = "is_scored", nullable = false)
    private boolean scored;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
