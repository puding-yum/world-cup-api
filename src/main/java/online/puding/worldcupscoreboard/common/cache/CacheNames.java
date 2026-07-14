package online.puding.worldcupscoreboard.common.cache;

/**
 * Nama cache Spring yang dipakai di anotasi {@code @Cacheable}/{@code @CacheEvict}.
 * Nama ini juga jadi prefix key di Redis. Selaras dengan daftar cache key di
 * spesifikasi ("Daftar endpoint backend").
 */
public final class CacheNames {

    private CacheNames() {
    }

    public static final String MATCHES_ONGOING = "matches:ongoing";
    public static final String MATCHES_UPCOMING = "matches:upcoming";
    public static final String MATCHES_ENDED = "matches:ended";
    public static final String MATCHES_NEXT = "matches:next";
    public static final String MATCHES_PREV = "matches:prev";
    public static final String MATCHES_DETAIL = "matches:detail"; // key = matchId
    public static final String BRACKET = "bracket";
    public static final String TEAMS = "teams";
    public static final String TEAMS_DETAIL = "teams:detail";
    public static final String TEAM_DETAIL = "team:detail";   // key = teamId
    public static final String PLAYER_DETAIL = "player:detail"; // key = playerId
}
