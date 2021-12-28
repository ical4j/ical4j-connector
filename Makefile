SHELL:=/bin/bash

.PHONY: all clean test build

all: build

clean:
	./gradlew clean

test:
	./gradlew test

build:
	./gradlew build

release:
	./gradlew release
