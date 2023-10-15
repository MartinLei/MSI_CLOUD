![example workflow](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/qualityCheck.yml/badge.svg?branch=main)

# Cloud Exercise1

## Purpose

You could upload files. See all uploaded files and also download wanted files.

# Run

1. Start the postgresql container at /dev-tools/posgresql/ ```$ docker compose up```
2. Run the play server with ```$ sbt run```$.

# REST API

See conf/routes for available endpoints.

# Used technologies
- scala3
- play
- slick

# Developing

## Linting

Use ```$ sbt scalafmt``` to automatically format all files.

## Debugging

Run server with ```sbt run -jvm-debug 9999``` and connect via remote debugger.

### Tutorials for scala and play with slick

[Slick db connection](https://blog.rockthejvm.com/slick/)

[REST API with play](https://blog.rockthejvm.com/play-framework-http-api-tutorial/)


