package online.puding.worldcupscoreboard.match.goal;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.match.MatchService;
import online.puding.worldcupscoreboard.match.goal.dto.GoalRequest;
import online.puding.worldcupscoreboard.match.goal.dto.GoalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);
    private static final String IDEM_PREFIX = "idem:goal:";

    private final MatchGoalRepository goalRepository;
    private final MatchService matchService;
    private final StringRedisTemplate redisTemplate;
    private final long idempotencyTtlSeconds;

    public GoalService(MatchGoalRepository goalRepository,
                       MatchService matchService,
                       StringRedisTemplate redisTemplate,
                       @Value("${app.idempotency.ttl-seconds:3600}") long idempotencyTtlSeconds) {
        this.goalRepository = goalRepository;
        this.matchService = matchService;
        this.redisTemplate = redisTemplate;
        this.idempotencyTtlSeconds = idempotencyTtlSeconds;
    }

    /**
     * Catat gol. Hanya boleh saat match ongoing. Kalau {@code idempotencyKey}
     * diberikan dan pernah dipakai, kembalikan gol yang sama tanpa membuat baru
     * (mencegah double-record akibat klik/retry ganda).
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId"),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ONGOING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public GoalResponse addGoal(Long matchId, GoalRequest request, String idempotencyKey) {
        matchService.requireOngoing(matchId);
        matchService.requireMatchTeam(matchId, request.matchTeamId());

        String idemKey = idempotencyKey == null || idempotencyKey.isBlank() ? null : IDEM_PREFIX + idempotencyKey;
        if (idemKey != null) {
            String existingId = redisTemplate.opsForValue().get(idemKey);
            if (existingId != null) {
                log.info("DB goalRepository.findById (idempotency {})", existingId);
                return goalRepository.findById(Long.valueOf(existingId))
                        .map(this::toResponse)
                        .orElseGet(() -> persist(matchId, request, idemKey));
            }
        }
        return persist(matchId, request, idemKey);
    }

    private GoalResponse persist(Long matchId, GoalRequest request, String idemKey) {
        MatchGoal goal = new MatchGoal();
        goal.setMatchTeamId(request.matchTeamId());
        goal.setPlayerId(request.playerId());
        goal.setMinute(request.minute());
        goal.setSecond(request.second());
        log.info("DB goalRepository.save (matchId={})", matchId);
        MatchGoal saved = goalRepository.save(goal);

        if (idemKey != null) {
            redisTemplate.opsForValue().set(idemKey, String.valueOf(saved.getId()),
                    Duration.ofSeconds(idempotencyTtlSeconds));
            log.info("REDIS set key={} (idempotency)", idemKey);
        }
        log.info("Gol dicatat: id={}, matchId={}, matchTeamId={}, playerId={}",
                saved.getId(), matchId, saved.getMatchTeamId(), saved.getPlayerId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId"),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ONGOING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public void deleteGoal(Long matchId, Long goalId) {
        log.info("DB goalRepository.findById ({})", goalId);
        MatchGoal goal = goalRepository.findById(goalId).orElseThrow(() -> new GoalNotFoundException(goalId));
        matchService.requireMatchTeam(matchId, goal.getMatchTeamId());
        log.info("DB goalRepository.delete ({})", goalId);
        goalRepository.delete(goal);
        log.info("Gol dihapus: id={}, matchId={}", goalId, matchId);
    }

    private GoalResponse toResponse(MatchGoal goal) {
        return new GoalResponse(goal.getId(), goal.getMatchTeamId(), goal.getPlayerId(),
                goal.getMinute(), goal.getSecond());
    }
}
