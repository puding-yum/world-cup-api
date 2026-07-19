package online.puding.worldcupscoreboard.match;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.common.exception.DomainException;
import online.puding.worldcupscoreboard.common.exception.ErrorCode;
import online.puding.worldcupscoreboard.common.util.AssetUrlBuilder;
import online.puding.worldcupscoreboard.match.card.MatchCard;
import online.puding.worldcupscoreboard.match.card.MatchCardRepository;
import online.puding.worldcupscoreboard.match.dto.MatchDetailResponse;
import online.puding.worldcupscoreboard.match.dto.MatchRequest;
import online.puding.worldcupscoreboard.match.dto.MatchSummaryResponse;
import online.puding.worldcupscoreboard.match.dto.MatchTeamScoreResponse;
import online.puding.worldcupscoreboard.match.dto.PenaltyKickResponse;
import online.puding.worldcupscoreboard.match.dto.TimelineEventResponse;
import online.puding.worldcupscoreboard.match.goal.MatchGoal;
import online.puding.worldcupscoreboard.match.goal.MatchGoalRepository;
import online.puding.worldcupscoreboard.match.penalty.MatchPenalty;
import online.puding.worldcupscoreboard.match.penalty.MatchPenaltyRepository;
import online.puding.worldcupscoreboard.team.Team;
import online.puding.worldcupscoreboard.team.TeamNotFoundException;
import online.puding.worldcupscoreboard.team.TeamRepository;
import online.puding.worldcupscoreboard.player.Player;
import online.puding.worldcupscoreboard.player.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchGoalRepository goalRepository;
    private final MatchCardRepository cardRepository;
    private final MatchPenaltyRepository penaltyRepository;
    private final AssetUrlBuilder assetUrls;

    public MatchService(MatchRepository matchRepository,
                        MatchTeamRepository matchTeamRepository,
                        TeamRepository teamRepository,
                        PlayerRepository playerRepository,
                        MatchGoalRepository goalRepository,
                        MatchCardRepository cardRepository,
                        MatchPenaltyRepository penaltyRepository,
                        AssetUrlBuilder assetUrls) {
        this.matchRepository = matchRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.goalRepository = goalRepository;
        this.cardRepository = cardRepository;
        this.penaltyRepository = penaltyRepository;
        this.assetUrls = assetUrls;
    }

    // ---------- Read: list (ter-cache) ----------
    // sync=true = request coalescing untuk endpoint TTL pendek + traffic tinggi

    @Cacheable(cacheNames = CacheNames.MATCHES_ONGOING, sync = true)
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> getOngoing() {
        return listByStatus(MatchStatus.ONGOING);
    }

    @Cacheable(cacheNames = CacheNames.MATCHES_UPCOMING)
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> getUpcoming() {
        return listByStatus(MatchStatus.UPCOMING);
    }

    @Cacheable(cacheNames = CacheNames.MATCHES_ENDED)
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> getEnded() {
        return listByStatus(MatchStatus.ENDED);
    }

    private List<MatchSummaryResponse> listByStatus(MatchStatus status) {
        List<Match> matches;
        if (status == MatchStatus.ENDED) {
            log.info("DB matchRepository.findByMatchStatusOrderByMatchDateDesc (status={})", status);
            matches = matchRepository.findByMatchStatusOrderByMatchDateDesc(status);
        } else {
            log.info("DB matchRepository.findByMatchStatusOrderByMatchDateAsc (status={})", status);
            matches = matchRepository.findByMatchStatusOrderByMatchDateAsc(status);
        }
        return assembleSummaries(matches);
    }

    /** GET /matches (admin) sengaja tidak di-cache. */
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> listAll() {
        log.info("DB matchRepository.findAllByOrderByMatchDateDesc");
        return assembleSummaries(matchRepository.findAllByOrderByMatchDateDesc());
    }

    @Cacheable(cacheNames = CacheNames.MATCHES_NEXT, sync = true)
    @Transactional(readOnly = true)
    public MatchSummaryResponse getNext() {
        log.info("DB matchRepository.findFirstByMatchStatusOrderByMatchDateAsc (UPCOMING)");
        return matchRepository.findFirstByMatchStatusOrderByMatchDateAsc(MatchStatus.UPCOMING)
                .map(match -> assembleSummaries(List.of(match)).get(0))
                .orElse(null);
    }

    @Cacheable(cacheNames = CacheNames.MATCHES_PREV, sync = true)
    @Transactional(readOnly = true)
    public MatchSummaryResponse getPrev() {
        log.info("DB matchRepository.findFirstByMatchStatusOrderByMatchDateDesc (ENDED)");
        return matchRepository.findFirstByMatchStatusOrderByMatchDateDesc(MatchStatus.ENDED)
                .map(match -> assembleSummaries(List.of(match)).get(0))
                .orElse(null);
    }

    @Cacheable(cacheNames = CacheNames.BRACKET)
    @Transactional(readOnly = true)
    public List<MatchSummaryResponse> getBracket() {
        log.info("DB matchRepository.findAllByOrderByMatchDateDesc (bracket)");
        List<Match> bracketMatches = matchRepository.findAllByOrderByMatchDateDesc().stream()
                .filter(m -> m.getBracketPosition() != null && !m.getBracketPosition().isBlank())
                .toList();
        return assembleSummaries(bracketMatches);
    }

    // ---------- Read: detail ----------

    @Cacheable(cacheNames = CacheNames.MATCHES_DETAIL, key = "#id", sync = true)
    @Transactional(readOnly = true)
    public MatchDetailResponse getDetail(Long id) {
        log.info("DB matchRepository.findById ({})", id);
        Match match = matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        log.info("DB matchTeamRepository.findByMatchIdOrderByIdAsc ({})", id);
        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchIdOrderByIdAsc(id);
        List<Long> matchTeamIds = matchTeams.stream().map(MatchTeam::getId).toList();

        Map<Long, Team> teamsById = loadTeams(matchTeams);
        log.info("DB goalRepository.countByMatchTeamIds (detail {})", id);
        Map<Long, Long> goalCounts = toCountMap(goalRepository.countByMatchTeamIds(orEmptyGuard(matchTeamIds)));
        log.info("DB penaltyRepository.countScoredByMatchTeamIds (detail {})", id);
        Map<Long, Long> penaltyScored = toCountMap(penaltyRepository.countScoredByMatchTeamIds(orEmptyGuard(matchTeamIds)));

        List<MatchGoal> goals;
        List<MatchCard> cards;
        List<MatchPenalty> penalties;
        if (matchTeamIds.isEmpty()) {
            goals = List.of();
            cards = List.of();
            penalties = List.of();
        } else {
            log.info("DB goalRepository.findByMatchTeamIdInOrderByMinuteAscSecondAsc (detail {})", id);
            goals = goalRepository.findByMatchTeamIdInOrderByMinuteAscSecondAsc(matchTeamIds);
            log.info("DB cardRepository.findByMatchTeamIdInOrderByMinuteAscSecondAsc (detail {})", id);
            cards = cardRepository.findByMatchTeamIdInOrderByMinuteAscSecondAsc(matchTeamIds);
            log.info("DB penaltyRepository.findByMatchTeamIdInOrderByKickOrderAsc (detail {})", id);
            penalties = penaltyRepository.findByMatchTeamIdInOrderByKickOrderAsc(matchTeamIds);
        }
        boolean hasShootout = !penalties.isEmpty();

        Map<Long, String> playerNames = loadPlayerNames(goals, cards, penalties);
        Map<Long, Long> teamIdByMatchTeam = matchTeams.stream()
                .collect(Collectors.toMap(MatchTeam::getId, MatchTeam::getTeamId));

        List<MatchTeamScoreResponse> teamScores = matchTeams.stream()
                .map(mt -> toTeamScore(mt, teamsById, goalCounts,
                        hasShootout ? (int) (long) penaltyScored.getOrDefault(mt.getId(), 0L) : null))
                .toList();

        List<TimelineEventResponse> timeline = buildTimeline(goals, cards, teamIdByMatchTeam, playerNames);
        List<PenaltyKickResponse> penaltyKicks = penalties.stream()
                .map(p -> new PenaltyKickResponse(p.getId(), p.getMatchTeamId(),
                        teamIdByMatchTeam.get(p.getMatchTeamId()), p.getKickOrder(), p.isScored(),
                        p.getPlayerId() == null ? null : playerNames.get(p.getPlayerId())))
                .toList();

        Long winnerTeamId = match.getMatchStatus() == MatchStatus.ENDED
                ? determineWinner(teamScores, hasShootout) : null;

        return new MatchDetailResponse(
                match.getId(), match.getMatchCode(), match.getBracketPosition(),
                match.getMatchDate(), match.getMatchStatus().value(),
                teamScores, timeline, hasShootout, penaltyKicks, winnerTeamId);
    }

    // ---------- Write (dengan invalidasi cache sesuai spec) ----------

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_UPCOMING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_NEXT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public MatchSummaryResponse create(MatchRequest request) {
        Match match = new Match();
        match.setMatchCode(request.matchCode());
        match.setBracketPosition(request.bracketPosition());
        match.setMatchDate(request.matchDate());
        match.setMatchStatus(parseStatus(request.status(), MatchStatus.UPCOMING));
        log.info("DB matchRepository.save (create)");
        Match saved = matchRepository.save(match);

        if (request.teamIds() != null && !request.teamIds().isEmpty()) {
            saveMatchTeams(saved.getId(), request.teamIds());
        }
        log.info("Match dibuat: id={}, status={}", saved.getId(), saved.getMatchStatus().value());
        return assembleSummaries(List.of(saved)).get(0);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.MATCHES_UPCOMING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ONGOING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ENDED, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_NEXT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_PREV, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public MatchSummaryResponse update(Long id, MatchRequest request) {
        log.info("DB matchRepository.findById ({})", id);
        Match match = matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        match.setMatchCode(request.matchCode());
        match.setBracketPosition(request.bracketPosition());
        match.setMatchDate(request.matchDate());
        if (request.status() != null) {
            match.setMatchStatus(parseStatus(request.status(), match.getMatchStatus()));
        }
        log.info("DB matchRepository.save (update {})", id);
        matchRepository.save(match);

        /*
         * Mengganti match_teams berarti menghapusnya, dan FK match_goals/match_cards/
         * match_penalties memakai ON DELETE CASCADE — semua gol, kartu, dan penalti
         * match ini ikut terhapus. Jadi bongkar-pasang HANYA kalau susunan timnya
         * benar-benar berubah; kalau sama, jangan disentuh. Tanpa ini, sekadar
         * mengedit jam kick-off lewat halaman admin (yang ikut mengirim teamIds)
         * akan menghapus seluruh event match tanpa peringatan.
         */
        if (request.teamIds() != null) {
            log.info("DB matchTeamRepository.findByMatchIdOrderByIdAsc (update {})", id);
            List<MatchTeam> existing = matchTeamRepository.findByMatchIdOrderByIdAsc(id);
            List<Long> currentTeamIds = existing.stream().map(MatchTeam::getTeamId).toList();
            if (!currentTeamIds.equals(request.teamIds())) {
                log.info("DB matchTeamRepository.deleteAll (update {})", id);
                matchTeamRepository.deleteAll(existing);
                if (!request.teamIds().isEmpty()) {
                    saveMatchTeams(id, request.teamIds());
                }
            }
        }
        log.info("Match diperbarui: id={}", id);
        return assembleSummaries(List.of(match)).get(0);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.MATCHES_UPCOMING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ONGOING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ENDED, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_NEXT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_PREV, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public MatchSummaryResponse changeStatus(Long id, String statusValue) {
        log.info("DB matchRepository.findById ({})", id);
        Match match = matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
        MatchStatus target = parseStatus(statusValue, null);
        if (target == null) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "status wajib diisi");
        }
        MatchStatus current = match.getMatchStatus();
        if (current != target && !isAllowedTransition(current, target)) {
            throw new DomainException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Transisi status dari " + current.value() + " ke " + target.value() + " tidak diizinkan");
        }
        match.setMatchStatus(target);
        log.info("DB matchRepository.save (changeStatus {})", id);
        matchRepository.save(match);
        log.info("Status match berubah: id={}, {} -> {}", id, current.value(), target.value());
        return assembleSummaries(List.of(match)).get(0);
    }

    /**
     * upcoming → ongoing → ended, plus ended → ongoing untuk membuka kembali match
     * yang sudah selesai (koreksi gol/kartu/penalti yang salah catat). Aturan "event
     * hanya boleh dicatat saat ongoing" tetap berlaku — admin membuka match dulu,
     * memperbaiki, lalu menutupnya lagi. upcoming tetap tidak bisa langsung ended,
     * supaya tidak ada match selesai yang tidak pernah dimainkan.
     */
    private boolean isAllowedTransition(MatchStatus from, MatchStatus to) {
        return (from == MatchStatus.UPCOMING && to == MatchStatus.ONGOING)
                || (from == MatchStatus.ONGOING && to == MatchStatus.ENDED)
                || (from == MatchStatus.ENDED && to == MatchStatus.ONGOING);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.MATCHES_DETAIL, key = "#id"),
            @CacheEvict(cacheNames = CacheNames.MATCHES_UPCOMING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ONGOING, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_ENDED, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_NEXT, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.MATCHES_PREV, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.BRACKET, allEntries = true)
    })
    @Transactional
    public void delete(Long id) {
        log.info("DB matchRepository.existsById ({})", id);
        if (!matchRepository.existsById(id)) {
            throw new MatchNotFoundException(id);
        }
        // FK ON DELETE CASCADE menghapus match_teams + semua event di bawahnya
        log.info("DB matchRepository.deleteById ({})", id);
        matchRepository.deleteById(id);
        log.info("Match dihapus: id={}", id);
    }

    private void saveMatchTeams(Long matchId, List<Long> teamIds) {
        List<Long> distinct = teamIds.stream().distinct().toList();
        if (distinct.size() != 2 || teamIds.size() != distinct.size()) {
            throw new DomainException(ErrorCode.INVALID_MATCH_TEAMS,
                    "Sebuah match harus punya tepat 2 tim yang berbeda");
        }
        for (Long teamId : distinct) {
            log.info("DB teamRepository.existsById ({})", teamId);
            if (!teamRepository.existsById(teamId)) {
                throw new TeamNotFoundException(teamId);
            }
            MatchTeam mt = new MatchTeam();
            mt.setMatchId(matchId);
            mt.setTeamId(teamId);
            log.info("DB matchTeamRepository.save (matchId={}, teamId={})", matchId, teamId);
            matchTeamRepository.save(mt);
        }
    }

    private MatchStatus parseStatus(String value, MatchStatus fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return MatchStatus.fromValue(value);
        } catch (IllegalArgumentException ex) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR,
                    "status harus salah satu dari: upcoming, ongoing, ended");
        }
    }

    // ---------- Assembly helpers ----------

    /** Rakit ringkasan banyak match dengan sedikit query batch (anti-N+1). */
    private List<MatchSummaryResponse> assembleSummaries(List<Match> matches) {
        if (matches.isEmpty()) {
            return List.of();
        }
        List<Long> matchIds = matches.stream().map(Match::getId).toList();
        log.info("DB matchTeamRepository.findByMatchIdInOrderByIdAsc ({} match)", matchIds.size());
        List<MatchTeam> matchTeams = matchTeamRepository.findByMatchIdInOrderByIdAsc(matchIds);
        List<Long> matchTeamIds = matchTeams.stream().map(MatchTeam::getId).toList();

        Map<Long, Team> teamsById = loadTeams(matchTeams);
        log.info("DB goalRepository.countByMatchTeamIds (summary)");
        Map<Long, Long> goalCounts = toCountMap(goalRepository.countByMatchTeamIds(orEmptyGuard(matchTeamIds)));
        // Skor adu penalti ikut dibawa di summary supaya bracket & schedule bisa
        // menampilkan "FT · 4–3 PENS" dan menentukan pemenang saat skor imbang.
        // Dua query agregat batch, bukan per-match (anti N+1).
        log.info("DB penaltyRepository.countKicksByMatchTeamIds (summary)");
        Map<Long, Long> penaltyKicks = toCountMap(penaltyRepository.countKicksByMatchTeamIds(orEmptyGuard(matchTeamIds)));
        log.info("DB penaltyRepository.countScoredByMatchTeamIds (summary)");
        Map<Long, Long> penaltyScored = toCountMap(penaltyRepository.countScoredByMatchTeamIds(orEmptyGuard(matchTeamIds)));
        Map<Long, List<MatchTeam>> teamsByMatch = matchTeams.stream()
                .collect(Collectors.groupingBy(MatchTeam::getMatchId));

        return matches.stream().map(match -> {
            List<MatchTeamScoreResponse> teams = teamsByMatch.getOrDefault(match.getId(), List.of()).stream()
                    .map(mt -> toTeamScore(mt, teamsById, goalCounts,
                            // null = tidak ada adu penalti; 0 = menendang tapi gagal semua
                            penaltyKicks.containsKey(mt.getId())
                                    ? (int) (long) penaltyScored.getOrDefault(mt.getId(), 0L)
                                    : null))
                    .toList();
            return new MatchSummaryResponse(match.getId(), match.getMatchCode(), match.getBracketPosition(),
                    match.getMatchDate(), match.getMatchStatus().value(), teams);
        }).toList();
    }

    private MatchTeamScoreResponse toTeamScore(MatchTeam mt, Map<Long, Team> teamsById,
                                               Map<Long, Long> goalCounts, Integer penaltyScore) {
        Team team = teamsById.get(mt.getTeamId());
        int score = (int) (long) goalCounts.getOrDefault(mt.getId(), 0L);
        return new MatchTeamScoreResponse(
                mt.getId(),
                mt.getTeamId(),
                team == null ? null : team.getName(),
                team == null ? null : team.getRegionMatchCode(),
                team == null ? null : assetUrls.flag(team.getRegionFlagCode()),
                score,
                penaltyScore);
    }

    private List<TimelineEventResponse> buildTimeline(List<MatchGoal> goals, List<MatchCard> cards,
                                                      Map<Long, Long> teamIdByMatchTeam,
                                                      Map<Long, String> playerNames) {
        Stream<TimelineEventResponse> goalEvents = goals.stream().map(g -> new TimelineEventResponse(
                g.getId(), "goal", g.getMinute(), g.getSecond(),
                teamIdByMatchTeam.get(g.getMatchTeamId()), g.getMatchTeamId(),
                g.getPlayerId() == null ? null : playerNames.get(g.getPlayerId()), null));
        Stream<TimelineEventResponse> cardEvents = cards.stream().map(c -> new TimelineEventResponse(
                c.getId(), "card", c.getMinute(), c.getSecond(),
                teamIdByMatchTeam.get(c.getMatchTeamId()), c.getMatchTeamId(),
                playerNames.get(c.getPlayerId()), c.getCardType()));
        // Urut kronologis: menit lalu detik (dua-duanya), null diletakkan di akhir
        Comparator<TimelineEventResponse> byTime = Comparator
                .comparing(TimelineEventResponse::minute, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TimelineEventResponse::second, Comparator.nullsLast(Comparator.naturalOrder()));
        return Stream.concat(goalEvents, cardEvents).sorted(byTime).toList();
    }

    private Long determineWinner(List<MatchTeamScoreResponse> teams, boolean hasShootout) {
        if (teams.size() != 2) {
            return null;
        }
        MatchTeamScoreResponse a = teams.get(0);
        MatchTeamScoreResponse b = teams.get(1);
        if (a.score() != b.score()) {
            return a.score() > b.score() ? a.teamId() : b.teamId();
        }
        if (hasShootout && a.penaltyScore() != null && b.penaltyScore() != null
                && !a.penaltyScore().equals(b.penaltyScore())) {
            return a.penaltyScore() > b.penaltyScore() ? a.teamId() : b.teamId();
        }
        return null;
    }

    private Map<Long, Team> loadTeams(List<MatchTeam> matchTeams) {
        Set<Long> teamIds = matchTeams.stream().map(MatchTeam::getTeamId).collect(Collectors.toSet());
        if (teamIds.isEmpty()) {
            return Map.of();
        }
        log.info("DB teamRepository.findAllById ({} tim)", teamIds.size());
        return teamRepository.findAllById(teamIds).stream()
                .collect(Collectors.toMap(Team::getId, Function.identity()));
    }

    private Map<Long, String> loadPlayerNames(List<MatchGoal> goals, List<MatchCard> cards,
                                              List<MatchPenalty> penalties) {
        Set<Long> playerIds = new java.util.HashSet<>();
        goals.forEach(g -> { if (g.getPlayerId() != null) playerIds.add(g.getPlayerId()); });
        cards.forEach(c -> playerIds.add(c.getPlayerId()));
        penalties.forEach(p -> { if (p.getPlayerId() != null) playerIds.add(p.getPlayerId()); });
        if (playerIds.isEmpty()) {
            return Map.of();
        }
        log.info("DB playerRepository.findAllById ({} pemain)", playerIds.size());
        return playerRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(Player::getId, Player::getName));
    }

    private Map<Long, Long> toCountMap(List<MatchTeamCount> counts) {
        return counts.stream().collect(Collectors.toMap(MatchTeamCount::getMatchTeamId, MatchTeamCount::getTotal));
    }

    /** Hindari query IN dengan koleksi kosong. */
    private List<Long> orEmptyGuard(List<Long> ids) {
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    // ---------- Helper untuk sub-domain event (goal/card/penalty) ----------

    /** Pastikan match ada dan berstatus ongoing, jika tidak lempar error yang sesuai. */
    @Transactional(readOnly = true)
    public Match requireOngoing(Long matchId) {
        log.info("DB matchRepository.findById (requireOngoing {})", matchId);
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new MatchNotFoundException(matchId));
        if (match.getMatchStatus() != MatchStatus.ONGOING) {
            throw new DomainException(ErrorCode.MATCH_NOT_ONGOING,
                    "Operasi hanya bisa dilakukan saat pertandingan berstatus ongoing");
        }
        return match;
    }

    /** Pastikan match_team milik match yang dimaksud (mencegah event salah match). */
    @Transactional(readOnly = true)
    public MatchTeam requireMatchTeam(Long matchId, Long matchTeamId) {
        log.info("DB matchTeamRepository.findById (requireMatchTeam {})", matchTeamId);
        MatchTeam matchTeam = matchTeamRepository.findById(matchTeamId)
                .orElseThrow(() -> new DomainException(ErrorCode.TEAM_NOT_IN_MATCH,
                        "match_team " + matchTeamId + " tidak ditemukan"));
        if (!matchTeam.getMatchId().equals(matchId)) {
            throw new DomainException(ErrorCode.TEAM_NOT_IN_MATCH,
                    "match_team " + matchTeamId + " bukan bagian dari match " + matchId);
        }
        return matchTeam;
    }

    /** Ambil match_team lewat id event yang match-nya harus cocok (dipakai saat DELETE event). */
    @Transactional(readOnly = true)
    public Optional<Match> findById(Long id) {
        log.info("DB matchRepository.findById ({})", id);
        return matchRepository.findById(id);
    }
}
