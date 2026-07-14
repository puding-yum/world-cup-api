package online.puding.worldcupscoreboard.match.penalty;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.match.penalty.dto.PenaltyRequest;
import online.puding.worldcupscoreboard.match.penalty.dto.PenaltyResponse;
import online.puding.worldcupscoreboard.stream.MatchEvent;
import online.puding.worldcupscoreboard.stream.SseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches/{matchId}/penalties")
public class PenaltyController {

    private final PenaltyService penaltyService;
    private final SseService sseService;

    public PenaltyController(PenaltyService penaltyService, SseService sseService) {
        this.penaltyService = penaltyService;
        this.sseService = sseService;
    }

    /*
     * SSE broadcast sengaja dilakukan di sini, bukan di dalam service. Transaksi
     * baru commit dan @CacheEvict baru jalan saat method service selesai, jadi
     * broadcast dari dalam service akan menyuruh client refetch selagi Redis
     * masih memegang data lama — client dapat data basi.
     */

    @PostMapping
    public ResponseEntity<BaseResponse<PenaltyResponse>> add(@PathVariable Long matchId,
                                                             @Valid @RequestBody PenaltyRequest request) {
        PenaltyResponse created = penaltyService.addPenalty(matchId, request);
        sseService.broadcast(MatchEvent.of(MatchEvent.PENALTY_ADDED, matchId, created.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @DeleteMapping("/{penaltyId}")
    public ResponseEntity<Void> delete(@PathVariable Long matchId, @PathVariable Long penaltyId) {
        penaltyService.deletePenalty(matchId, penaltyId);
        sseService.broadcast(MatchEvent.of(MatchEvent.PENALTY_REMOVED, matchId, penaltyId));
        return ResponseEntity.noContent().build();
    }
}
