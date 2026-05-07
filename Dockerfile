FROM alpine:latest

RUN apk add --no-cache openjdk21 maven
#nikhil
WORKDIR /app
COPY pom.xml .
COPY src ./src

CMD ["mvn","clean","test"]