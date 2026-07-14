package online.puding.worldcupscoreboard.match;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.match.dto.MatchDetailResponse;
import online.puding.worldcupscoreboard.match.dto.MatchRequest;
import online.puding.worldcupscoreboard.match.dto.MatchSummaryResponse;
import online.puding.worldcupscoreboard.match.dto.StatusRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import online.puding.worldcupscoreboard.stream.MatchEvent;
import online.puding.worldcupscoreboard.stream.SseService;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchService matchService;
    private final SseService sseService;

    public MatchController(MatchService matchService, SseService sseService) {
        this.matchService = matchService;
        this.sseService = sseService;
    }

    @GetMapping("/ongoing")
    public BaseResponse<List<MatchSummaryResponse>> ongoing() {
        return BaseResponse.success(matchService.getOngoing());
    }

    @GetMapping("/upcoming")
    public BaseResponse<List<MatchSummaryResponse>> upcoming() {
        return BaseResponse.success(matchService.getUpcoming());
    }

    @GetMapping("/ended")
    public BaseResponse<List<MatchSummaryResponse>> ended() {
        return BaseResponse.success(matchService.getEnded());
    }

    @GetMapping("/next")
    public BaseResponse<MatchSummaryResponse> next() {
        return BaseResponse.success(matchService.getNext());
    }

    @GetMapping("/prev")
    public BaseResponse<MatchSummaryResponse> prev() {
        return BaseResponse.success(matchService.getPrev());
    }

    @GetMapping("/{id}")
    public BaseResponse<MatchDetailResponse> detail(@PathVariable Long id) {
        return BaseResponse.success(matchService.getDetail(id));
    }

    @GetMapping
    public BaseResponse<List<MatchSummaryResponse>> list() {
        return BaseResponse.success(matchService.listAll());
    }

    @PostMapping
    public ResponseEntity<BaseResponse<MatchSummaryResponse>> create(@Valid @RequestBody MatchRequest request) {
        MatchSummaryResponse created = matchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @PutMapping("/{id}")
    public BaseResponse<MatchSummaryResponse> update(@PathVariable Long id, @Valid @RequestBody MatchRequest request) {
        return BaseResponse.success(matchService.update(id, request));
    }

    /*
     * SSE broadcast sengaja dilakukan di sini, bukan di dalam service. Transaksi
     * baru commit dan @CacheEvict baru jalan saat method service selesai, jadi
     * broadcast dari dalam service akan menyuruh client refetch selagi Redis
     * masih memegang data lama — client dapat data basi.
     */
    @PatchMapping("/{id}/status")
    public BaseResponse<MatchSummaryResponse> changeStatus(@PathVariable Long id,
                                                           @Valid @RequestBody StatusRequest request) {
        MatchSummaryResponse updated = matchService.changeStatus(id, request.status());
        sseService.broadcast(MatchEvent.of(MatchEvent.STATUS_CHANGED, id, id));
        return BaseResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
