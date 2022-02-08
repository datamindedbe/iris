FROM python:3.8-slim-buster

# Create non-root user
RUN useradd -ms /bin/bash python-api
USER python-api

# Copy the source files into the container
WORKDIR /repo
COPY . /repo

# Install pip requirements
RUN python -m pip install -r requirements.txt

# Define the command to be run 
# when the container is started
EXPOSE 8080
ENTRYPOINT ["python"]
CMD ["main.py"]