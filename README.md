![example workflow](https://github.com/MartinLei/MSI_CLOUD/actions/workflows/qualityCheck.yml/badge.svg?branch=main)

# Cloud Exercise1

## Purpose

You could upload files. See all uploaded files and also download wanted files.

# Run

1. Start the postgresql container at /dev-tools/posgresql/ ```$ docker compose up```
2. Run the play server with ```$ sbt run```$.

# Developing

## Linting

Use ```$ sbt scalafmt``` to automatically format all files.

### Tutorials for scala and play

[Slick db connection](https://blog.rockthejvm.com/slick/)

[REST API with play](https://blog.rockthejvm.com/play-framework-http-api-tutorial/)

