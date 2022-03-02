# Kafka

## References

- [kafka quickstart](https://kafka.apache.org/quickstart)
- [kafka python client](https://docs.confluent.io/clients-confluent-kafka-python/current/overview.html)

## Getting started 
In this example we will produce messages to a Kafka topic and consume the messages back with multiple consumers



#### Setting up Kafka broker and Zookeeper
```bash
cd workspace
docker-compose up
```

## Exercises
1) Use the Kafka CLI (available in the path (eg: kafka-topics.sh --list --bootstrap-server localhost:9092):
   1) Create new topic called 'my-events'
   2) Produce some messages using the 'kafka-console-producer'
   3) Consume the messages using the 'kafka-console-producer'
2) Take a look at the Python producer and Consumer
   1) Create a new topic called 'my_first_topic' with 3 partitions
   2) Run the producer.py file to write some messages to Kafka
   3) Run the consumer.py file to consume those messages
   4) Run a second instance of the consumer.py file. This should trigger a rebalance. There are now 2 consumers in the same group. One consumer will get 1 partition assigned, the other one will get 2 partitions. Try to see this effect in action by producing and consuming
   5) Update the 'group.id' property in the consumer.py file and run the consumer. What happens now? Why? 