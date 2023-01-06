import sys

from elasticsearch import Elasticsearch
import random


es = Elasticsearch("http://localhost:9200/", basic_auth=("elastic", "changeme"))
es.info()

fruits = ["banana", "apple", "kiwi", "orange"]
colors = ["red", "blue", "yellow", "green"]

def add_fruit():
    fruit = {
        'kind': random.choice(fruits),
        'color': random.choice(colors),
        'dimensions': {
            'weight': random.randint(1,10),
            'size': random.randint(1,10)
        }
    }
    es.index(index='fruit',document=fruit)

def add_multiple_fruits(n: int):
    for i in range(n):
        add_fruit()

if __name__ == "__main__":
    n = int(sys.argv[1])
    add_multiple_fruits(n)