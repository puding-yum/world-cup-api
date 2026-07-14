package online.puding.worldcupscoreboard.match.penalty;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;
import online.puding.worldcupscoreboard.match.MatchService;
import online.puding.worldcupscoreboard.match.penalty.dto.PenaltyRequest;
import online.puding.worldcupscoreboard.match.penalty.dto.PenaltyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PenaltyService {

    private static final Logger log = LoggerFactory.getLogger(PenaltyService.class);

    private final MatchPenaltyRepository penaltyRepository;
    private final MatchService matchService;

    public PenaltyService(MatchPenaltyRepository penaltyRepository,
                          MatchService matchService) {
        this.penaltyRepository = penaltyRepository;
        this.matchService = matchService;
    }

    @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId")
    @Transactional
    public PenaltyResponse addPenalty(Long matchId, PenaltyRequest request) {
        matchService.requireOngoing(matchId);
        matchService.requireMatchTeam(matchId, request.matchTeamId());

        // Aturan bisnis: satu kick_order per tim tidak boleh dipakai dua kali
        if (penaltyRepository.existsByMatchTeamIdAndKickOrder(request.matchTeamId(), request.kickOrder())) {
            throw new DomainException(ErrorCode.PENALTY_ORDER_TAKEN,
                    "kick_order " + request.kickOrder() + " sudah dipakai untuk tim ini");
        }

        MatchPenalty penalty = new MatchPenalty();
        penalty.setMatchTeamId(request.matchTeamId());
        penalty.setPlayerId(request.playerId());
        penalty.setKickOrder(request.kickOrder());
        penalty.setScored(Boolean.TRUE.equals(request.scored()));
        MatchPenalty saved = penaltyRepository.save(penalty);

        log.info("Penalti dicatat: id={}, matchId={}, kickOrder={}, scored={}",
                saved.getId(), matchId, saved.getKickOrder(), saved.isScored());
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#matchId")
    @Transactional
    public void deletePenalty(Long matchId, Long penaltyId) {
        MatchPenalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new PenaltyNotFoundException(penaltyId));
        matchService.requireMatchTeam(matchId, penalty.getMatchTeamId());
        penaltyRepository.delete(penalty);
        log.info("Penalti dihapus: id={}, matchId={}", penaltyId, matchId);
    }

    private PenaltyResponse toResponse(MatchPenalty penalty) {
        return new PenaltyResponse(penalty.getId(), penalty.getMatchTeamId(), penalty.getPlayerId(),
                penalty.getKickOrder(), penalty.isScored());
    }
}
