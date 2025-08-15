FROM ubuntu:jammy AS build
WORKDIR /build
RUN apt update -y && apt install -y --no-install-recommends git openjdk-8-jdk vim && rm -rf /var/lib/apt/lists/*
COPY . /build/intellij-elm

FROM build AS build_exec
RUN cd intellij-elm && ./gradlew buildPlugin

FROM scratch
COPY --from=build_exec /build/intellij-elm/build/distributions/ /

