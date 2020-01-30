FROM alpine:3.11 AS builder
WORKDIR /retro-game-src
COPY . .
RUN \
  # Install packages needed to build the game.
  apk update && \
  apk --no-cache add \
    cmake \
    gcc \
    make \
    maven \
    musl-dev \
    openjdk11-jdk && \
  # Build the battle engine.
  mkdir build && \
  cd build && \
  JAVA_HOME=/usr/lib/jvm/java-11-openjdk cmake -DCMAKE_BUILD_TYPE=Release ../battle-engine && \
  make && \
  cd .. && \
  # Build the game.
  mvn -B package

FROM alpine:3.11
WORKDIR /retro-game
COPY --from=0 /retro-game-src/build/libBattleEngine.so .
COPY --from=0 /retro-game-src/target/retro-game-*.jar retro-game.jar
RUN \
  # Install packages needed to run the game.
  apk update && \
  apk --no-cache add openjdk11-jre-headless && \
  # Change the permissions of the artifacts.
  chmod 400 *
CMD ["java", "-Djava.library.path=.", "-jar", "retro-game.jar"]
