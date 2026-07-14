package online.puding.worldcupscoreboard.team;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.common.util.AssetUrlBuilder;
import online.puding.worldcupscoreboard.player.Player;
import online.puding.worldcupscoreboard.player.PlayerRepository;
import online.puding.worldcupscoreboard.player.dto.PlayerResponse;
import online.puding.worldcupscoreboard.team.dto.TeamDetailResponse;
import online.puding.worldcupscoreboard.team.dto.TeamRequest;
import online.puding.worldcupscoreboard.team.dto.TeamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final AssetUrlBuilder assetUrls;

    public TeamService(TeamRepository teamRepository,
                       PlayerRepository playerRepository,
                       AssetUrlBuilder assetUrls) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.assetUrls = assetUrls;
    }

    @Cacheable(cacheNames = CacheNames.TEAMS)
    @Transactional(readOnly = true)
    public List<TeamResponse> listTeams() {
        return teamRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Semua tim beserta pemainnya. Anti-N+1: 1 query untuk tim, 1 query batch
     * untuk semua pemain, lalu digabung di memori.
     */
    @Cacheable(cacheNames = CacheNames.TEAMS_DETAIL)
    @Transactional(readOnly = true)
    public List<TeamDetailResponse> listTeamsDetail() {
        List<Team> teams = teamRepository.findAllByOrderByNameAsc();
        if (teams.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        Map<Long, List<PlayerResponse>> playersByTeam =
                playerRepository.findByTeamIdInOrderByTeamIdAscJerseyNumberAsc(teamIds).stream()
                        .collect(Collectors.groupingBy(
                                Player::getTeamId,
                                Collectors.mapping(this::toPlayerResponse, Collectors.toList())));
        return teams.stream()
                .map(team -> toDetail(team, playersByTeam.getOrDefault(team.getId(), List.of())))
                .toList();
    }

    @Cacheable(cacheNames = CacheNames.TEAM_DETAIL, key = "#id")
    @Transactional(readOnly = true)
    public TeamDetailResponse getTeamDetail(Long id) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new TeamNotFoundException(id));
        List<PlayerResponse> players = playerRepository.findByTeamIdOrderByJerseyNumberAsc(id).stream()
                .map(this::toPlayerResponse)
                .toList();
        return toDetail(team, players);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true)
    })
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Team team = new Team();
        apply(team, request);
        Team saved = teamRepository.save(team);
        log.info("Tim dibuat: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAM_DETAIL, key = "#id")
    })
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new TeamNotFoundException(id));
        apply(team, request);
        Team saved = teamRepository.save(team);
        log.info("Tim diperbarui: id={}", saved.getId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAM_DETAIL, key = "#id")
    })
    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new TeamNotFoundException(id);
        }
        teamRepository.deleteById(id);
        log.info("Tim dihapus: id={}", id);
    }

    private void apply(Team team, TeamRequest request) {
        team.setName(request.name());
        team.setRegionMatchCode(request.regionMatchCode());
        team.setRegionFlagCode(request.regionFlagCode());
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getRegionMatchCode(),
                team.getRegionFlagCode(),
                assetUrls.flag(team.getRegionFlagCode()));
    }

    private TeamDetailResponse toDetail(Team team, List<PlayerResponse> players) {
        return new TeamDetailResponse(
                team.getId(),
                team.getName(),
                team.getRegionMatchCode(),
                team.getRegionFlagCode(),
                assetUrls.flag(team.getRegionFlagCode()),
                players);
    }

    private PlayerResponse toPlayerResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getTeamId(),
                player.getJerseyNumber(),
                player.getPosition(),
                assetUrls.avatar(player.getName()));
    }
}
