# animalProtect app

## Purpose

Core service.

# REST API

See conf/routes for available endpoints.


## DEV
```bash
# run
sbt ";project animalProtectApp;~run"
```

## Linting

Use ```$ sbt scalafmt``` to automatically format all files.

## Debugging

Run server with ```sbt -jvm-debug 5005``` and connect via remote debugger.

### Tutorials for scala and play with slick

[Slick db connection](https://blog.rockthejvm.com/slick/)

[REST API with play](https://blog.rockthejvm.com/play-framework-http-api-tutorial/)


