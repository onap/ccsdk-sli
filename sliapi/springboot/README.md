This directory contains a demo springboot implementation of the SLI-API healthcheck method.

To start this server, run:

java -jar -DserviceLogicDirectory=src/main/resources target/sliapi-springboot-{version}.jar

This will start a servlet on port 8080.  To test to that servlet, post a blank
message to that port:

curl http://127.0.0.1:8080/SLI-API:healthcheck -X POST -H "Content-Type: application/json"