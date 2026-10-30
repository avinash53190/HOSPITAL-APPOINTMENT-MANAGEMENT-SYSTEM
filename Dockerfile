FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app/backend

# Copy backend source code and dependencies
COPY backend/pom.xml .
COPY backend/src ./src

# Set default port
ENV PORT=8080
EXPOSE 8080

# Run Embedded Apache Tomcat runner
CMD ["mvn", "compile", "exec:java"]

