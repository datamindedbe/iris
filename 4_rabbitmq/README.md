# RabbitMQ

## References

- [rabbitmq tutorial](https://www.rabbitmq.com/tutorials/tutorial-one-python.html)

## Getting started 
In this example we will produce weather forecasts to a rabbitMQ exchange



#### Setting up Kafka broker and Zookeeper
```bash
cd workspace
docker-compose up
```

## Exercises
1) Run the weather_producer.py file to start producing weather forecasts every 5 seconds
2) Write a rabbitmq_consumer that consumes the messages produced on the 'weather1' queue
3) Update the producer to a fanout exchange and bind 3 new queues to it. Run the producer again and see if all queues have messages
   1) fanout1
   2) fanout2
   3) fanout3
