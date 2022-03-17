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

#### Destroying the environment
```bash
docker-compose down
```
Important to ensure no residual network interfaces remain

### Exercise
Create a Storm topology that splits sentences in individual words, counts the number of words in a 10 second tumbling window and writes the result to a file.
