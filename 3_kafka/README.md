# Kafka

## References

- [kafka quickstart](https://kafka.apache.org/quickstart)

## Getting started 
In this example we will produce messages to a Kafka topic and consume the messages back with multiple consumers



#### Setting up Kafka broker and Zookeeper
```bash
cd broker
docker-compose up
```

## Exercises
1) Use the Kafka CLI (available in the path (eg: kafka-topics.sh --list --bootstrap-server localhost:9092):
   1) Create new topic called 'my-events'
   2) Produce some messages using the 'kafka-console-producer'
   3) Consume the messages using the 'kafka-console-producer'
2) TODO