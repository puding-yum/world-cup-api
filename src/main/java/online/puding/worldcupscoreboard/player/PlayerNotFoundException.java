package online.puding.worldcupscoreboard.player;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class PlayerNotFoundException extends DomainException {

    public PlayerNotFoundException(Long id) {
        super(ErrorCode.PLAYER_NOT_FOUND, "Pemain dengan id " + id + " tidak ditemukan");
    }
}
