package online.puding.worldcupscoreboard.stream;

/**
 * Event mentah yang dikirim lewat SSE (tidak dibungkus BaseResponse).
 * Frontend memakai {@code type} + {@code matchId} untuk memutuskan refetch/patch.
 */
public record MatchEvent(String type, Long matchId, Long entityId) {

    public static final String GOAL_ADDED = "goal.added";
    public static final String GOAL_REMOVED = "goal.removed";
    public static final String CARD_ADDED = "card.added";
    public static final String CARD_REMOVED = "card.removed";
    public static final String PENALTY_ADDED = "penalty.added";
    public static final String PENALTY_REMOVED = "penalty.removed";
    public static final String STATUS_CHANGED = "status.changed";

    public static MatchEvent of(String type, Long matchId, Long entityId) {
        return new MatchEvent(type, matchId, entityId);
    }
}
