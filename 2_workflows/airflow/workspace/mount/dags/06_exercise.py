import datetime as dt

from airflow import DAG
from airflow.operators.bash import BashOperator
from airflow.operators.python import PythonOperator
from random import random
import time

dag = DAG(
    dag_id="Exercise",
    schedule_interval="@daily",
    start_date=dt.datetime(2022, 2, 15),
)


def sleep_random():
    time.sleep(random() * 10)


with dag:
    task_1 = PythonOperator(
        task_id='sleep',
        python_callable=sleep_random,
    )
    task_2 = BashOperator(
        task_id='print_date', bash_command="date")
    task_3 = BashOperator(
        task_id='Hello_world',
        bash_command='echo "hello world"',
    )

    [task_1, task_2] >> task_3
