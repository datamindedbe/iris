import sys

from confluent_kafka import Consumer, KafkaException, KafkaError, Message

running = True


def run():
    conf = {'bootstrap.servers': "localhost:9092",
            'group.id': "bar",
            'auto.offset.reset': 'earliest'}

    consumer = Consumer(conf)
    topic = 'my_first_topic'
    basic_consume_loop(consumer, [topic])


def basic_consume_loop(consumer, topics):
    try:
        consumer.subscribe(topics)

        while running:
            msg = consumer.poll(timeout=1.0)
            if msg is None: continue

            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    # End of partition event
                    sys.stderr.write('%% %s [%d] reached end at offset %d\n' %
                                     (msg.topic(), msg.partition(), msg.offset()))
                elif msg.error():
                    raise KafkaException(msg.error())
            else:
                msg_process(msg)
    finally:
        # Close down consumer to commit final offsets.
        consumer.close()


def msg_process(message: Message):
    print("Key", message.key())
    print("Value", message.value())
    print("Partition", message.partition())
    print("Offset", message.offset())
    print("Timestamp", message.timestamp())
    print("\n\n")


def shutdown():
    global running
    running = False


if __name__ == '__main__':
    run()
