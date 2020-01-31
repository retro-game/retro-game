# Retro Game
An OGame clone in retro style

[![CI](https://github.com/retro-game/retro-game/workflows/CI/badge.svg)](https://github.com/retro-game/retro-game/actions)

## Running
You can run retro-game on Docker Compose:
```shell script
docker-compose up
```
This will setup a development environment, where you can test the game.
The server should be running on [http://127.0.0.1:8080](http://127.0.0.1:8080).

Note that this setup is not ready for production!
This setup may expose ports to database and redis.
Make sure you configure it properly before running on production.
Check [etc](etc) directory if you are looking for systemd service and nginx configs.

## Community & server
There is a testing server: [https://retro-game.org](https://retro-game.org).

If you want to chat about the game or development, see: [https://discord.gg/FJ6rK4V](https://discord.gg/FJ6rK4V).
