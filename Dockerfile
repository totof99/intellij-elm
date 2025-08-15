FROM ubuntu:jammy AS build
WORKDIR /build
RUN apt update -y && apt install -y --no-install-recommends git openjdk-8-jdk vim curl gzip && rm -rf /var/lib/apt/lists/*
RUN curl -L -o elm.gz https://github.com/elm/compiler/releases/download/0.19.1/binary-for-linux-64-bit.gz && gunzip elm.gz && chmod +x elm && mv elm /usr/local/bin/
COPY . /build/intellij-elm

FROM build AS build_exec
RUN cd intellij-elm && ./gradlew check && ./gradlew buildPlugin

FROM scratch
COPY --from=build_exec /build/intellij-elm/build/distributions/ /

