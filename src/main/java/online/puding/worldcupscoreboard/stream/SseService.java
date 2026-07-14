package online.puding.worldcupscoreboard.stream;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry koneksi SSE. Menyimpan semua {@link SseEmitter} yang hidup dan
 * mem-broadcast event ke semuanya. Emitter yang mati otomatis dibersihkan.
 */
@Service
public class SseService {

    private static final Logger log = LoggerFactory.getLogger(SseService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final long timeoutMs;

    public SseService(@Value("${app.sse.timeout-ms:3600000}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ex) {
            emitters.remove(emitter);
        }
        log.debug("SSE client terhubung; total koneksi={}", emitters.size());
        return emitter;
    }

    public void broadcast(MatchEvent event) {
        for (SseEmitter emitter : emitters) {
            send(emitter, SseEmitter.event().name(event.type()).data(event));
        }
        log.info("SSE broadcast type={}, matchId={}, penerima={}", event.type(), event.matchId(), emitters.size());
    }

    /**
     * Heartbeat: komentar SSE (baris diawali ':') yang diabaikan browser, tapi
     * membuat ada byte lewat secara berkala. Tanpa ini, koneksi yang menganggur
     * lama (tidak ada gol/kartu) diputus diam-diam oleh nginx, Cloudflare,
     * firewall, atau NAT — dan frontend mengira dirinya masih tersambung.
     *
     * Sekaligus jadi penyapu koneksi mati: tanpa heartbeat, emitter milik tab
     * yang sudah ditutup baru ketahuan mati saat broadcast gol berikutnya, jadi
     * registry bisa menumpuk koneksi hantu dan {@link #activeConnections()}
     * berbohong.
     *
     * Interval harus JAUH di bawah idle-timeout perantara terkecil
     * (Cloudflare ~100 detik, nginx proxy_read_timeout default 60 detik).
     */
    @Scheduled(fixedDelayString = "${app.sse.heartbeat-ms:20000}")
    public void heartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            send(emitter, SseEmitter.event().comment("ping"));
        }
        log.debug("SSE heartbeat dikirim ke {} koneksi", emitters.size());
    }

    private void send(SseEmitter emitter, SseEmitter.SseEventBuilder payload) {
        try {
            emitter.send(payload);
        } catch (Exception ex) {
            // Klien sudah pergi (tab ditutup, jaringan putus) — buang koneksinya.
            emitter.completeWithError(ex);
            emitters.remove(emitter);
        }
    }

    @PreDestroy
    void closeAll() {
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }

    public int activeConnections() {
        return emitters.size();
    }
}
