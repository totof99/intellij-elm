FROM ubuntu:jammy AS build
WORKDIR /build
RUN apt update -y && apt install -y --no-install-recommends git openjdk-17-jdk vim && rm -rf /var/lib/apt/lists/*
COPY . /build/intellij-elm
RUN cd intellij-elm && ./gradlew buildPlugin

FROM scratch
COPY --from=build /build/intellij-elm/build/distributions/ /

