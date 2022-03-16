from time import sleep
from json import dumps
import logging
from random import randint
from kafka import KafkaProducer
import socket
from kafka.admin import KafkaAdminClient, NewTopic


def run():
    admin_client = KafkaAdminClient(
        bootstrap_servers="kafka:9092",
        client_id='test'
    )

    topic_list = []
    topic_list.append(NewTopic(name="output-topic", num_partitions=1, replication_factor=1))
    admin_client.create_topics(new_topics=topic_list, validate_only=False)
    producer = KafkaProducer(bootstrap_servers=['kafka:9092'],
                             value_serializer=lambda x:
                             dumps(x).encode('utf-8'))
    while True:
        value = randint(0,100)
        data = {'number' : value}
        print("Sending message", flush=True)
        producer.send('input-topic', value=data)
        sleep(1)


if __name__ == '__main__':
    run()