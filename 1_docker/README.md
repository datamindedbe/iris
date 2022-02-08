# Docker

In this training we introduce docker as a convenient way to support the rest of the training. 

## References

- [docker home](https://www.docker.com/)
- [docker installation](https://docs.docker.com/get-docker/)
- [docker compose](https://docs.docker.com/compose/)
- [docker cheat sheet](https://www.docker.com/sites/default/files/d8/2019-09/docker-cheat-sheet.pdf)
- [docker hub](https://hub.docker.com/)

## Getting started 

You can either install docker on your local machine or use the online IDE [gitpod](https://www.gitpod.io/). Installing docker on Windows and running the exercises requires the use of WSL2.

### Sharing images

1. Pull and image from a registry `docker pull ubuntu:focal` and `docker pull wordpress:latest`
2. Retag a local image with a new image name and tag  `docker tag ubuntu:focal mylinux:ubuntu`
3. List images available on your local machine `docker image ls`

### Running a container from an existing imange

1. Run a container, execute a single command and delete the container after `docker run --rm ubuntu:focal echo "Hello"`
2. Run a container in interactive mode and delete the container after `docker run -ti --rm ubuntu:focal /bin/bash` (Tip: type `exit` to terminate)
3. Run a container in the background with a service and expose a port on the host machine`docker run -d -p 8080:80 --name wordpress wordpress`
4. List running containers `docker container ls`
5. List the logs of a container `docker container logs --tail 100 wordpress`
6. Stop a container with SIGTERM `docker container stop wordpress` or with SIGKILL `docker container kill wordpress`
7. Remove a container `docker container rm wordpress`

### Building your own container image

1. Build the greeter_shell image `docker build -t greeter_shell:latest`
2. Run the greeter_shell image `docker run greeter_shell:latest`
3. Build the greeter_python image `docker build -t greeter_python:latest`
4. Run the greeter_python image `docker run --expose 1994:8080 greeter_python:latest`
5. Run the greeter_python image `docker run --expose --volume /TODO/config:/repo/config 1994:8080 greeter_python:latest`

### Using docker compose to start multiple services and allow them to communicate 

1. Run wordpress by using `docker-compose up -d`