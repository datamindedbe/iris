# Workflow management and rules engines

## References

- [airflow home](https://airflow.apache.org/)
- [easy rules home](https://github.com/j-easy/easy-rules)

## Getting started 
In this example we will have a producer publishe messages on an event bus (rabbitmq). Those messages are picked up by the rules engine (easy rules) and when a rule is met, it will trigger a workflow (airflow).

### Worklfow management (Apache Airflow)

#### Setting up airflow env
```bash
cd airflow
docker-compose up
```

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