from confluent_kafka import Producer
import socket


def run():
    conf = {'bootstrap.servers': "localhost:9092",
            'client.id': socket.gethostname()}
    topic = 'my_first_topic'
    producer = Producer(conf)

    for i in range(1, 10):
        print('Producing message with key', i)
        producer.produce(topic, key=str(i), value=f"value {i}")

    producer.flush()  # Typically, flush() should be called prior to shutting down the producer to ensure all outstanding/queued/in-flight messages are delivered.


if __name__ == '__main__':
    run()
