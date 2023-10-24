FROM hseeberger/scala-sbt:eclipse-temurin-17.0.2_1.6.2_3.1.1
RUN apt-get update && apt-get install
WORKDIR /MSI_CLOUD
ADD .. /MSI_CLOUD
CMD sbt "run -Dconfig.resource=prod.conf"



