package online.puding.worldcupscoreboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling} dibutuhkan oleh heartbeat SSE (SseService#heartbeat). */
@SpringBootApplication
@EnableScheduling
public class WorldcupscoreboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorldcupscoreboardApplication.class, args);
	}

}
