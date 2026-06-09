ARG DOCKER_REGISTRY
ARG MAVEN_IMAGE
ARG JRE_IMAGE

FROM ${DOCKER_REGISTRY}${MAVEN_IMAGE} AS builder

WORKDIR /app

COPY pom.xml ./
COPY settings.xml /root/.m2/settings.xml

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

FROM ${DOCKER_REGISTRY}${JRE_IMAGE}

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9030

ENTRYPOINT ["java", "-jar", "app.jar"]
