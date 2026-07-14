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

# Tanpa ini JVM di container bisa salah membaca memori host dan kena OOM-kill.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
