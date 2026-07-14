package online.puding.worldcupscoreboard.match.card;

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

/** Kartu. {@code matchTeamId} = tim penerima kartu. card_type: yellow/red/second_yellow_red. */
@Entity
@Table(name = "match_cards")
@Getter
@Setter
@NoArgsConstructor
public class MatchCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "match_team_id", nullable = false)
    private Long matchTeamId;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(name = "minute")
    private Integer minute;

    @Column(name = "second")
    private Integer second;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
