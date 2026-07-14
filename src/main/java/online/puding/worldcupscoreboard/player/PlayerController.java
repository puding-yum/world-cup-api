package online.puding.worldcupscoreboard.player;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.player.dto.PlayerDetailResponse;
import online.puding.worldcupscoreboard.player.dto.PlayerRequest;
import online.puding.worldcupscoreboard.player.dto.PlayerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/{id}")
    public BaseResponse<PlayerDetailResponse> get(@PathVariable Long id) {
        return BaseResponse.success(playerService.getPlayer(id));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<PlayerResponse>> create(@Valid @RequestBody PlayerRequest request) {
        PlayerResponse created = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @PutMapping("/{id}")
    public BaseResponse<PlayerResponse> update(@PathVariable Long id, @Valid @RequestBody PlayerRequest request) {
        return BaseResponse.success(playerService.updatePlayer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
