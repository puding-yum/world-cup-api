package online.puding.worldcupscoreboard.player;

import online.puding.worldcupscoreboard.common.cache.CacheNames;
import online.puding.worldcupscoreboard.common.util.AssetUrlBuilder;
import online.puding.worldcupscoreboard.player.dto.PlayerDetailResponse;
import online.puding.worldcupscoreboard.player.dto.PlayerRequest;
import online.puding.worldcupscoreboard.player.dto.PlayerResponse;
import online.puding.worldcupscoreboard.team.Team;
import online.puding.worldcupscoreboard.team.TeamNotFoundException;
import online.puding.worldcupscoreboard.team.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final AssetUrlBuilder assetUrls;

    public PlayerService(PlayerRepository playerRepository,
                         TeamRepository teamRepository,
                         AssetUrlBuilder assetUrls) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.assetUrls = assetUrls;
    }

    @Cacheable(cacheNames = CacheNames.PLAYER_DETAIL, key = "#id")
    @Transactional(readOnly = true)
    public PlayerDetailResponse getPlayer(Long id) {
        log.info("DB playerRepository.findById ({})", id);
        Player player = playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
        log.info("DB teamRepository.findById ({})", player.getTeamId());
        Team team = teamRepository.findById(player.getTeamId())
                .orElseThrow(() -> new TeamNotFoundException(player.getTeamId()));
        return toDetail(player, team);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAM_DETAIL, allEntries = true)
    })
    @Transactional
    public PlayerResponse createPlayer(PlayerRequest request) {
        requireTeamExists(request.teamId());
        Player player = new Player();
        apply(player, request);
        log.info("DB playerRepository.save (create)");
        Player saved = playerRepository.save(player);
        log.info("Pemain dibuat: id={}, name={}, teamId={}", saved.getId(), saved.getName(), saved.getTeamId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAM_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PLAYER_DETAIL, key = "#id")
    })
    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        log.info("DB playerRepository.findById ({})", id);
        Player player = playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
        requireTeamExists(request.teamId());
        apply(player, request);
        log.info("DB playerRepository.save (update {})", id);
        Player saved = playerRepository.save(player);
        log.info("Pemain diperbarui: id={}", saved.getId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TEAMS_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TEAM_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PLAYER_DETAIL, key = "#id")
    })
    @Transactional
    public void deletePlayer(Long id) {
        log.info("DB playerRepository.existsById ({})", id);
        if (!playerRepository.existsById(id)) {
            throw new PlayerNotFoundException(id);
        }
        log.info("DB playerRepository.deleteById ({})", id);
        playerRepository.deleteById(id);
        log.info("Pemain dihapus: id={}", id);
    }

    private void requireTeamExists(Long teamId) {
        log.info("DB teamRepository.existsById ({})", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new TeamNotFoundException(teamId);
        }
    }

    private void apply(Player player, PlayerRequest request) {
        player.setName(request.name());
        player.setTeamId(request.teamId());
        player.setJerseyNumber(request.jerseyNumber());
        player.setPosition(request.position());
    }

    private PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getTeamId(),
                player.getJerseyNumber(),
                player.getPosition(),
                assetUrls.avatar(player.getName()));
    }

    private PlayerDetailResponse toDetail(Player player, Team team) {
        return new PlayerDetailResponse(
                player.getId(),
                player.getName(),
                player.getJerseyNumber(),
                player.getPosition(),
                assetUrls.avatar(player.getName()),
                team.getId(),
                team.getName(),
                assetUrls.flag(team.getRegionFlagCode()));
    }
}
