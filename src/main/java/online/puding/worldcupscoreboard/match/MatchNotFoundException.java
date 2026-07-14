package online.puding.worldcupscoreboard.match;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class MatchNotFoundException extends DomainException {

    public MatchNotFoundException(Long id) {
        super(ErrorCode.MATCH_NOT_FOUND, "Pertandingan dengan id " + id + " tidak ditemukan");
    }
}
