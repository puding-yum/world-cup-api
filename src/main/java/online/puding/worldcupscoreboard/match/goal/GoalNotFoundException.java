package online.puding.worldcupscoreboard.match.goal;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class GoalNotFoundException extends DomainException {

    public GoalNotFoundException(Long id) {
        super(ErrorCode.GOAL_NOT_FOUND, "Gol dengan id " + id + " tidak ditemukan");
    }
}
