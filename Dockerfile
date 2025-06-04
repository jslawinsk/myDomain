# Importing JDK and copying required files
FROM maven:3.8.4-openjdk-17 AS build
# FROM openjdk:17-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src src

# Copy Maven wrapper
# COPY mvnw .
# COPY .mvn .mvn

# Set execution permission for the Maven wrapper
# RUN chmod +x ./mvnw
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image using OpenJDK 17
FROM openjdk:17-jdk
VOLUME /tmp

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080