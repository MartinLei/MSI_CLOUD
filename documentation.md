# Documentation - CloudBox
CloudBox ist eine Web-Anwendung, um Medien-Dateien (Bilder, Audios, Videos) upzuloaden. Die hochgeladenen Dateien werden persistent gespeichert.

### Features:
CloudBox hat folgende Funktionalitäten:
- Upload von Mediendateien
- Download von Mediendateien
- Suchfunktion für eine bestimmte Mediendatei inkl. der Anzeige von Zusatzinformationen

## Application Architecture

### Betriebssystem

### Verwendete Sprachen:
Als Programmiersprachen werden in CloudBox überwiegend Scala3 und JavaScript verwendet. Außerdem HTML und CSS für das Frontend.

### Technologie-Stack
Im Rahmen der Web-Applikation werden folgende Frameworks verwendet:
- Scala Play für Backend und Frontend
- Docker für einen Datenbankcontainer
- PostgreeSQL-Datenbank
- Slick-Bibliothek für die Einbindung der Datenbank in Scala3

### Datenbank
Als Speicher für die Mediendateien wird eine relationale Datenbank verwendet. Dabei wird auf eine PostgreeSQL-Datenbank zurückgegriffen. 
Sie wird mithilfe eines Docker-Containers betrieben, welcher in einem Compose-File definiert ist.
Die Einbindung in Scala wird mithilfe der Bibliothek Slick erreicht. 
Um weniger Netzwerkverkehr zu erreichen, wird im Backend mit DataTranferObjects gearbeitet. 
So wird beispielsweise zur Anzeige der vorhandenen Dateien nicht immer die ganze Mediendatei bewegt.

### REST-API
Mithilfe der Scala-Routes-Datei werden sog. Routen mit den verfügbaren Endpunkten definiert. Somit ist es möglich, 
HTTP-Anfragen an die entsprechende Methode im Backend weiterzuleiten.

### Qualitätssicherung
Zur Sicherung der Qualität sind für die Controller-Klassen Scala-Tests implementiert. 
Für die Code-Qualität wird das Linting-Tool ```$ scalafmt ``` zuhilfe genommen.

### Runtime Environment
Das System, auf dem die Webapplikation gestartet werden soll, muss folgende Anforderungen erfüllen:
- Für das Starten der CloubBox-Web-Applikation ist eine Java Virtual Machine (JVM) notwendig.
- Außerdem sollte ```$ sbt ``` installiert sein, um das Scala-Play-Projekt zu bauen.
- Docker muss installiert sein, um die Datenbank in einem Container zu starten.

## Run
Zum Starten der Web-Anwendung sind folgende Schritte notwendig:
1. Der Docker Container mit der PostgreeSQL-Datenbank kann aus dem Ordner /dev-tools/posgresql/ heraus mit ```$ docker compose up``` gestartet werden.
2. Der Scala-Play-Server kann mit ```$ sbt run``` gestartet werden.
3. In einem Browser die Adresse ```$ localhost:9000``` aufrufen