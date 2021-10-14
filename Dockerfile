FROM debian:bullseye-slim AS builder
WORKDIR /retro-game-src
COPY . .
RUN \
  # Install packages needed to build the game.
  apt-get update && \
  apt-get install -y \
    build-essential \
    cmake \
    maven \
    openjdk-17-jdk && \
  rm -rf /var/lib/apt/lists/* && \
  # Build the battle engine.
  JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))" cmake -B build -DCMAKE_BUILD_TYPE=Release battle-engine && \
  cmake --build build && \
  # Build the game.
  mvn -B -DskipTests package && \
  rm -rf ~/.m2

FROM debian:bullseye-slim
WORKDIR /retro-game
COPY --from=0 /retro-game-src/build/libBattleEngine.so .
COPY --from=0 /retro-game-src/target/retro-game-*.jar retro-game.jar
RUN \
  # Install packages needed to run the game.
  apt-get update && \
  apt-get install -y openjdk-17-jre-headless && \
  rm -rf /var/lib/apt/lists/* && \
  # Change the permissions of the artifacts.
  chmod 400 *
CMD ["java", "-Djava.library.path=.", "-jar", "retro-game.jar"]
