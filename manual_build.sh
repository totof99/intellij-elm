#!/bin/sh

sudo docker build --target build . -t build_intellij-elm && sudo docker run -it --rm --workdir /build/intellij-elm build_intellij-elm 