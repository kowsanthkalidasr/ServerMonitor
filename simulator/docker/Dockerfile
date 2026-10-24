# ============================================================
# Stage 1: Build
# ============================================================

FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build


COPY docker/libs/grpc-proto-1.0.0-SNAPSHOT.jar ./libs/



COPY simulator/mvnw simulator/mvnw.cmd ./
COPY simulator/.mvn ./.mvn
RUN chmod +x mvnw

RUN ./mvnw install:install-file \
    -Dfile=libs/grpc-proto-1.0.0-SNAPSHOT.jar \
    -DgroupId=com.serMon \
    -DartifactId=grpc-proto \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=jar -B

COPY simulator/pom.xml ./simulator/
RUN ./mvnw dependency:resolve -f simulator/pom.xml -B

COPY simulator/src/ ./simulator/src/
RUN ./mvnw clean package -f simulator/pom.xml -DskipTests -B

# ============================================================
# Stage 2: Runtime
# ============================================================

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /build/simulator/target/simulator-*.jar simulator.jar

EXPOSE  9080 9090

# Added: Entrypoint to execute the fat JAR
ENTRYPOINT ["java", "-jar", "simulator.jar"]