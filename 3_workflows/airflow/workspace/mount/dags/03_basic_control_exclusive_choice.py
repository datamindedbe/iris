import datetime as dt
import random

from airflow import DAG
from airflow.operators.dummy import DummyOperator
from airflow.operators.python import BranchPythonOperator

dag = DAG(
    dag_id="Exclusive_choice",
    schedule_interval="@daily",
    start_date=dt.datetime(2022, 2, 15),
)


def random_branch(task_1: str, task_2: str):
    i = random.randint(1,5)
    if i >= 3:
        return task_1
    else:
        return task_2

with dag:
    task_1 = DummyOperator(task_id="task_1")
    task_2 = DummyOperator(task_id="task_2")
    task_3 = DummyOperator(task_id="task_3")
    branch = BranchPythonOperator(task_id="branch_task"
                                  , python_callable=lambda : random_branch(task_2.task_id, task_3.task_id)
                                  )

    task_1 >> branch >> [task_2,task_3]

