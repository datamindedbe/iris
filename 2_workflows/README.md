#Environment for workflows training

###Setting up airflow env
```bash
cd airflow
docker-compose up
```
###Creating a RabbitMQ and temperature producer
```bash
cd rabbitmq
docker-compose up
```

###Running easy-rules
```bash
cd easy-rules
make run
```