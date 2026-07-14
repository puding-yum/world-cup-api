package online.puding.worldcupscoreboard.match.card;

import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;

public class CardNotFoundException extends DomainException {

    public CardNotFoundException(Long id) {
        super(ErrorCode.CARD_NOT_FOUND, "Kartu dengan id " + id + " tidak ditemukan");
    }
}
