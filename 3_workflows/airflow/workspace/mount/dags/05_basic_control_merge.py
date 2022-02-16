import datetime as dt

from airflow import DAG
from airflow.operators.dummy import DummyOperator

dag = DAG(
    dag_id="Merge",
    schedule_interval="@daily",
    start_date=dt.datetime(2022, 2, 15),
)




with dag:
    task_1 = DummyOperator(task_id="task_1")
    task_2 = DummyOperator(task_id="task_2")
    task_3 = DummyOperator(task_id="task_3", trigger_rule="one_success")

    [task_1, task_2] >> task_3