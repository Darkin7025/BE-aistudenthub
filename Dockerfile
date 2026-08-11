# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Tối ưu hóa bộ nhớ khi build Maven
ENV MAVEN_OPTS="-Xmx1024m -XX:+UseSerialGC"
# Download dependencies first to cache them
RUN mvn dependency:go-offline -B
# Copy the source code and build the application
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Tối ưu hóa JVM cho gói Free (512MB RAM, 0.1 CPU)
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss256k", "-XX:MaxRAMPercentage=50.0", "-XX:MaxMetaspaceSize=100m", "-XX:ActiveProcessorCount=1", "-jar", "app.jar"]