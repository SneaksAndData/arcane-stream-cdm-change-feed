FROM --platform=$BUILDPLATFORM sbtscala/scala-sbt:eclipse-temurin-23.0.1_11_1.10.5_3.5.2 AS build
ARG GITHUB_TOKEN
ENV GITHUB_TOKEN=$GITHUB_TOKEN

WORKDIR /build

COPY . .

RUN GITHUB_TOKEN=$GITHUB_TOKEN sbt assembly

FROM --platform=$BUILDPLATFORM gcr.io/distroless/java-base-debian12

COPY --from=build /build/target/scala-3.6.1 /app

CMD ["java", "-jar", "/app/com.sneaksanddata.arcane.cdm-change-feed.assembly.jar"]
