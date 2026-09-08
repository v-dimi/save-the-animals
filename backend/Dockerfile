# --- build the jar ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Resolve dependencies against the pom alone, so editing sources does not re-download them.
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run in the developer's environment and in CI; the image build just packages.
RUN mvn -B -q clean package -DskipTests

# --- run it -----------------------------------------------------------------
FROM eclipse-temurin:21-jre

# curl is only here for the compose healthcheck.
RUN apt-get update \
    && apt-get install --no-install-recommends -y curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --create-home --uid 10001 appuser \
    && mkdir -p /data \
    && chown appuser:appuser /data

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

USER appuser
ENV APP_DB_PATH=/data/app.db
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
