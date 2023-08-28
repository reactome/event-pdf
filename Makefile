docker-pull:
	docker pull maven:3.8.6-openjdk-11-slim

.PHONY: build-image
build-image: docker-pull \
             $(call print-help,build, "Build the docker image.")
	docker build -t reactome/event-pdf:latest .

.PHONY: run-image
run-image: $(call print-help,run, "Run the docker image.")
	docker run reactome/event-pdf:latest -v $(pwd)/output:/output --net=host
