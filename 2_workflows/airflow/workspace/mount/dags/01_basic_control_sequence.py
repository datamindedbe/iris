import datetime as dt

from airflow import DAG
from airflow.operators.dummy import DummyOperator

dag = DAG(
    dag_id="Sequence",
    schedule_interval="@daily",
    start_date=dt.datetime(2022, 2, 15),
)




with dag:
    task_1 = DummyOperator(task_id="task_1")
    task_2 = DummyOperator(task_id="task_2")

    task_1 >> task_2
