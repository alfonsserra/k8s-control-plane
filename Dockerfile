### STAGE 1: Build ###

# We label our stage as 'builder'
FROM maven:alpine as builder

## Storing node modules on a separate layer will prevent unnecessary npm installs at each build
RUN mkdir /k8s-control-plane

WORKDIR /k8s-control-plane

COPY . .

## Build the angular app in production mode and store the artifacts in dist folder
RUN mvn package

### STAGE 2: Setup ###

FROM openjdk:8-jre-alpine


COPY --from=builder /k8s-control-plane/target/k8s-control-plane-1.0.jar k8s-control-plane.jar


CMD ["java","-jar","k8s-control-plane.jar"]
