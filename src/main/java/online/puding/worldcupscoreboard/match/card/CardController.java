package online.puding.worldcupscoreboard.match.card;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.match.card.dto.CardRequest;
import online.puding.worldcupscoreboard.match.card.dto.CardResponse;
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
@RequestMapping("/matches/{matchId}/cards")
public class CardController {

    private final CardService cardService;
    private final SseService sseService;

    public CardController(CardService cardService, SseService sseService) {
        this.cardService = cardService;
        this.sseService = sseService;
    }

    /*
     * SSE broadcast sengaja dilakukan di sini, bukan di dalam service. Transaksi
     * baru commit dan @CacheEvict baru jalan saat method service selesai, jadi
     * broadcast dari dalam service akan menyuruh client refetch selagi Redis
     * masih memegang data lama — client dapat data basi.
     */

    @PostMapping
    public ResponseEntity<BaseResponse<CardResponse>> add(@PathVariable Long matchId,
                                                          @Valid @RequestBody CardRequest request) {
        CardResponse created = cardService.addCard(matchId, request);
        sseService.broadcast(MatchEvent.of(MatchEvent.CARD_ADDED, matchId, created.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable Long matchId, @PathVariable Long cardId) {
        cardService.deleteCard(matchId, cardId);
        sseService.broadcast(MatchEvent.of(MatchEvent.CARD_REMOVED, matchId, cardId));
        return ResponseEntity.noContent().build();
    }
}
