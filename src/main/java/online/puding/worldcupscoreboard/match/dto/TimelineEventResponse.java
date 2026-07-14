package online.puding.worldcupscoreboard.match.dto;

/**
 * Satu baris timeline (gabungan gol & kartu, urut menit lalu detik).
 * {@code type} = "goal" atau "card". {@code cardType} hanya terisi untuk kartu.
 */
public record TimelineEventResponse(
        Long id,
        String type,
        Integer minute,
        Integer second,
        Long teamId,
        Long matchTeamId,
        String playerName,
        String cardType) {
}
