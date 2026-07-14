package online.puding.worldcupscoreboard.stream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class StreamController {

    private final SseService sseService;

    public StreamController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Header di sini bukan hiasan — tanpa ini SSE mati diam-diam di produksi:
     *
     * - {@code X-Accel-Buffering: no} menyuruh nginx TIDAK mem-buffer response ini.
     *   Secara default nginx menahan output sampai buffer penuh, sementara SSE
     *   menetes sedikit-sedikit dan tidak pernah "penuh" — jadi event nyangkut di
     *   proxy dan skor di layar penonton tidak pernah berubah. Header ini bekerja
     *   per-response, jadi tetap aman walau {@code proxy_buffering off} lupa dipasang.
     * - {@code Cache-Control: no-cache} mencegah proxy/CDN meng-cache stream.
     *
     * Tidak ada error apa pun kalau ini hilang — itulah yang bikin susah dilacak.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(sseService.register());
    }
}
