package online.puding.worldcupscoreboard.team;

import jakarta.validation.Valid;
import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.team.dto.TeamDetailResponse;
import online.puding.worldcupscoreboard.team.dto.TeamRequest;
import online.puding.worldcupscoreboard.team.dto.TeamResponse;
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

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public BaseResponse<List<TeamResponse>> list() {
        return BaseResponse.success(teamService.listTeams());
    }

    @GetMapping("/detail")
    public BaseResponse<List<TeamDetailResponse>> listDetail() {
        return BaseResponse.success(teamService.listTeamsDetail());
    }

    @GetMapping("/{id}")
    public BaseResponse<TeamDetailResponse> get(@PathVariable Long id) {
        return BaseResponse.success(teamService.getTeamDetail(id));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<TeamResponse>> create(@Valid @RequestBody TeamRequest request) {
        TeamResponse created = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(created));
    }

    @PutMapping("/{id}")
    public BaseResponse<TeamResponse> update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return BaseResponse.success(teamService.updateTeam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
