import pika
from dataclasses import dataclass, asdict
import random
from time import sleep
import json


@dataclass
class WeatherForecast:
    """Class for forecasting the weather"""
    weather: str
    degrees: float

    def to_json(self) -> str:
        return json.dumps(asdict(self))


def run():
    print("Setting up RabbitMQ connection")
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    exchange_name = "weather-exchange"
    queue_name = "weather1"
    routing_key = "weather"

    # Make Exchange
    channel.exchange_declare(exchange=exchange_name, exchange_type="direct", durable=True)

    # Make queue
    channel.queue_declare(queue=queue_name, durable=True)

    # Bind queue to exchange with routing key
    channel.queue_bind(queue=queue_name, exchange=exchange_name, routing_key=routing_key)

    print("Starting the producer of weather forecasts")
    while True:
        forecast: WeatherForecast = generate_weather_forecast()
        print("Producing weather forecast...")
        channel.basic_publish(exchange=exchange_name, routing_key=routing_key, body=bytes(forecast.to_json(), "utf-8"))
        sleep(5)


def generate_weather_forecast() -> WeatherForecast:
    forecast_types = ["sunny", "rainy", "snowy", "cloudy"]
    return WeatherForecast(weather=random.choice(forecast_types),
                           degrees=random.uniform(-5.0, 30))


if __name__ == '__main__':
    run()
