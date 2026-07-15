# Multi-stage: build pakai Maven, runtime cuma JRE (image jauh lebih kecil dan
# tidak membawa toolchain build ke produksi).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependency di-cache dulu: selama pom.xml tidak berubah, layer ini tidak
# di-download ulang tiap build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Jangan jalan sebagai root.
RUN useradd --system --uid 1001 worldcup
USER worldcup

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# Persen dihitung dari batas memori CONTAINER (mem_limit di compose), bukan RAM
# host. 60% dari limit 1200m = ~720m heap, menyisakan ~480m untuk non-heap JVM
# (metaspace, code cache, thread stack Tomcat). Di VPS 2 GB sisanya ~800m + swap
# cukup untuk OS + nginx + Docker daemon. JANGAN naikkan ke 75% di box 2 GB.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
