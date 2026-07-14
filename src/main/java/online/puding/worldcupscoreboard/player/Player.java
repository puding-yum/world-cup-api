package online.puding.worldcupscoreboard.player;

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
 * Relasi ke tim disimpan sebagai {@code teamId} polos (bukan asosiasi JPA)
 * untuk menghindari lazy loading yang diam-diam memicu N+1; join antar tim &
 * pemain dilakukan eksplisit di query/di memori.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "position")
    private String position;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
