# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Tối ưu hóa bộ nhớ khi build Maven (tăng lên 512m để biên dịch nhanh hơn)
ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"
# Sử dụng cache mount để tránh tải lại dependencies mỗi lần build
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B
# Copy the source code and build the application
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B -Dmaven.compiler.fork=false

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss256k", "-Xms128m", "-Xmx200m", "-XX:MaxMetaspaceSize=80m", "-XX:ReservedCodeCacheSize=48m", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]