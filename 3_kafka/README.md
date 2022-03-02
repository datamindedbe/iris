# Kafka

## References

- [kafka quickstart](https://kafka.apache.org/quickstart)

## Getting started 
In this example we will have a producer publishe messages on an event bus (rabbitmq). Those messages are picked up by the rules engine (easy rules) and when a rule is met, it will trigger a workflow (airflow).

### Worklfow management (Apache Airflow)

#### Setting up airflow env
```bash
cd airflow
docker-compose up
```
username:password = airflow:airflow

### Rules engine

### Creating a RabbitMQ and temperature producer
```bash
cd rabbitmq
docker-compose up
```

### Running easy-rules
```bash
cd easy-rules
make run
```

## Exercises
1) Create a new DAG with 3 tasks:
   1) Sleep for 10 seconds
   2) Print the current time
   3) Print "hello world" after the previous 2 tasks succeeded
2) Create a new rule that triggers your pipeline when the temperature exceeds 25 degrees. You might need to adjust the priority of previous rules