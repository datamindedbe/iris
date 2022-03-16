# Apache storm

## References

- Storm applied

## Getting started
In this demo we set up a basic Apache Storm environment through gradle and docker compose
#### Building topologies
```bash
cd storm-nimbus/code
gradle build
```
During building of the docker image, the jars will be packaged inside the image
#### Spinning up the environment
```bash
docker-compose up --build
```