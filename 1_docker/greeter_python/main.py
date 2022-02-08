import json
from fastapi import FastAPI
import uvicorn

app = FastAPI()

CONFIG_PATH = "config/config.json"

with open(CONFIG_PATH, 'r') as f:
    config = json.load(f)

AUTHOR = config.get("AUTHOR")
COURSES = config.get("LIST_OF_COURSES")

@app.get("/hello")
def say_hello():
    return f"Hello from {AUTHOR}"

@app.get("/courses")
def get_list_of_courses():
    return f"Here are the courses available by {AUTHOR}: {COURSES}"

if __name__ == '__main__':
    uvicorn.run(app, port=8080, host='0.0.0.0')