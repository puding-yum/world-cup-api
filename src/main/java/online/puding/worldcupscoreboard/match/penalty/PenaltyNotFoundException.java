package online.puding.worldcupscoreboard.match.penalty;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class PenaltyNotFoundException extends DomainException {

    public PenaltyNotFoundException(Long id) {
        super(ErrorCode.PENALTY_NOT_FOUND, "Penalti dengan id " + id + " tidak ditemukan");
    }
}
