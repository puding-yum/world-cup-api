package online.puding.worldcupscoreboard.team;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class TeamNotFoundException extends DomainException {

    public TeamNotFoundException(Long id) {
        super(ErrorCode.TEAM_NOT_FOUND, "Tim dengan id " + id + " tidak ditemukan");
    }
}
