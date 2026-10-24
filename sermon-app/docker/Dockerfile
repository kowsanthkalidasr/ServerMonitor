# ============================================================
# Stage 1: Build
# ============================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build


COPY docker/libs/grpc-proto-1.0.0-SNAPSHOT.jar ./libs/


# Copy Maven wrapper and POM first (layer-cache friendly)
COPY sermon-app/mvnw sermon-app/mvnw.cmd ./
COPY sermon-app/.mvn ./.mvn
RUN chmod +x mvnw


RUN ./mvnw install:install-file \
    -Dfile=libs/grpc-proto-1.0.0-SNAPSHOT.jar \
    -DgroupId=com.serMon \
    -DartifactId=grpc-proto \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=jar -B


COPY sermon-app/pom.xml ./sermon-app/
RUN ./mvnw dependency:resolve -f sermon-app/pom.xml -B

COPY sermon-app/src  ./sermon-app/src
RUN ./mvnw clean package -f sermon-app/pom.xml -DskipTests -B


# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=builder /build/sermon-app/target/SerMon-*.jar sermon-app.jar

# Non-root user for security
#RUN addgroup --system sermon && adduser --system --ingroup sermon sermon
#USER sermon

EXPOSE 9000 9010

ENTRYPOINT ["java", "-jar", "sermon-app.jar"]
