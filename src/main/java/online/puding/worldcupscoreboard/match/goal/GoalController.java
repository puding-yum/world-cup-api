package online.puding.worldcupscoreboard.match.goal;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.match.goal.dto.GoalRequest;
import online.puding.worldcupscoreboard.match.goal.dto.GoalResponse;
import online.puding.worldcupscoreboard.stream.MatchEvent;
import online.puding.worldcupscoreboard.stream.SseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches/{matchId}/goals")
public class GoalController {

    private final GoalService goalService;
    private final SseService sseService;

    public GoalController(GoalService goalService, SseService sseService) {
        this.goalService = goalService;
        this.sseService = sseService;
    }

    /*
     * SSE broadcast sengaja dilakukan di sini, bukan di dalam service. Transaksi
     * baru commit dan @CacheEvict baru jalan saat method service selesai, jadi
     * broadcast dari dalam service akan menyuruh client refetch selagi Redis
     * masih memegang data lama — client dapat data basi.
     */
    @PostMapping
    public ResponseEntity<BaseResponse<GoalResponse>> add(
            @PathVariable Long matchId,
            @Valid @RequestBody GoalRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        GoalResponse created = goalService.addGoal(matchId, request, idempotencyKey);
        sseService.broadcast(MatchEvent.of(MatchEvent.GOAL_ADDED, matchId, created.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> delete(@PathVariable Long matchId, @PathVariable Long goalId) {
        goalService.deleteGoal(matchId, goalId);
        sseService.broadcast(MatchEvent.of(MatchEvent.GOAL_REMOVED, matchId, goalId));
        return ResponseEntity.noContent().build();
    }
}
