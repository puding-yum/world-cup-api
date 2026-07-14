package online.puding.worldcupscoreboard.match;

import online.puding.worldcupscoreboard.common.response.BaseResponse;
import online.puding.worldcupscoreboard.match.dto.MatchSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Bracket = match yang punya bracket_position, dengan skor & status masing-masing. */
@RestController
@RequestMapping("/bracket")
public class BracketController {

    private final MatchService matchService;

    public BracketController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public BaseResponse<List<MatchSummaryResponse>> bracket() {
        return BaseResponse.success(matchService.getBracket());
    }
}
