package online.puding.worldcupscoreboard.match.card;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.match.MatchService;
import online.puding.worldcupscoreboard.match.card.dto.CardRequest;
import online.puding.worldcupscoreboard.match.card.dto.CardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final MatchCardRepository cardRepository;
    private final MatchService matchService;

    public CardService(MatchCardRepository cardRepository,
                       MatchService matchService) {
        this.cardRepository = cardRepository;
        this.matchService = matchService;
    }

    @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId")
    @Transactional
    public CardResponse addCard(Long matchId, CardRequest request) {
        matchService.requireOngoing(matchId);
        matchService.requireMatchTeam(matchId, request.matchTeamId());

        MatchCard card = new MatchCard();
        card.setMatchTeamId(request.matchTeamId());
        card.setPlayerId(request.playerId());
        card.setCardType(request.cardType());
        card.setMinute(request.minute());
        card.setSecond(request.second());
        log.info("DB cardRepository.save (matchId={})", matchId);
        MatchCard saved = cardRepository.save(card);

        log.info("Kartu dicatat: id={}, matchId={}, type={}, playerId={}",
                saved.getId(), matchId, saved.getCardType(), saved.getPlayerId());
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId")
    @Transactional
    public void deleteCard(Long matchId, Long cardId) {
        log.info("DB cardRepository.findById ({})", cardId);
        MatchCard card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException(cardId));
        matchService.requireMatchTeam(matchId, card.getMatchTeamId());
        log.info("DB cardRepository.delete ({})", cardId);
        cardRepository.delete(card);
        log.info("Kartu dihapus: id={}, matchId={}", cardId, matchId);
    }

    private CardResponse toResponse(MatchCard card) {
        return new CardResponse(card.getId(), card.getMatchTeamId(), card.getPlayerId(),
                card.getCardType(), card.getMinute(), card.getSecond());
    }
}
