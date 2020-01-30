# Retro Game
An OGame clone in retro style

[![CI](https://github.com/retro-game/retro-game/workflows/CI/badge.svg)](https://github.com/retro-game/retro-game/actions)

## Requirements
* JDK 11 (OpenJDK works fine)
* Maven >= 3
* PostgreSQL >= 9.3
* Redis
* CMake >= 3.1
* C99 compiler, e.g. gcc >= 4.5

## Basic instalation

### Setup database
Create a new database:
```
createdb retro-game
```
Initialize the database:
```
psql -d retro-game -f sql/schema.sql
```

### Configure
Configure the _config/application.properties_ file.

### Build battle engine
Create a new working directory:
```
mkdir build
cd build
```
Run CMake to find dependencies and generate Makefiles.
You can specify -G option if you want to use a different generator.
```
cmake -DCMAKE_BUILD_TYPE=Release ../battle-engine
```
Compile the battle engine:
```
make
```
Now you should have a dynamic library _libBattleEngine.so_ or _BattleEngine.dll_ inside _build_ directory.

### Build project
```
mvn package
```
Now you should have _retro-game-V.VV.V.jar_ file inside _target_ directory, where _V.VV.V_ is the current version.

### Run
Run the Retro Game, it will load the configuration from _config/application.properties_ file.
```
java -Djava.library.path=build -jar target/retro-game-V.VV.V.jar
```
Go to [http://127.0.0.1:8080](http://127.0.0.1:8080).

### Use another web server to serve static files (optional)
Retro Game forces browsers to revalidate cache every request, there is nothing done to handle static files properly.
Thus, browsers won't load the pages very fast.
You probably want to serve static files using another web server.
See [etc/nginx.conf](etc/nginx.conf) for a sample configuration for [nginx](https://www.nginx.org/).

### Add more skins (optional)
You may want to add more skins, put them into _src/main/resources/public/static/skins_ directory and modify the config file appropriately.

## Community & server

There is a testing server: [https://retro-game.org](https://retro-game.org).

If you want to chat about the game or development, see: [https://discord.gg/FJ6rK4V](https://discord.gg/FJ6rK4V).
