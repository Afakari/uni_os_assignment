#!/bin/bash

IMAGE_NAME="rmi-distributed-system"
CONTAINER_NAME="rmi-distributed-system-container"

echo "Building Docker image: $IMAGE_NAME"
docker build -t $IMAGE_NAME .

if [ $? -ne 0 ]; then
  echo "Error: Docker image build failed"
  exit 1
fi

echo "Starting container: $CONTAINER_NAME"
docker run --name $CONTAINER_NAME \
  -v $(pwd)/registry.policy:/app/registry.policy \
  -p 1099:1099 \
  --rm \
  $IMAGE_NAME \
  /bin/bash -c "
    # Start the registry service in the background
    java -Djava.rmi.server.hostname=localhost \
         -Djava.security.manager \
         -Djava.security.policy=/app/registry.policy \
         -cp /app.jar \
         --add-exports java.rmi/sun.rmi.registry=ALL-UNNAMED \
         shared.CustomRegistry &

    # Wait briefly to allow registry to initialize
    sleep 5

    # Start the worker services in the background
    java -cp /app.jar worker.WorkerApp localhost worker1 &
    java -cp /app.jar worker.WorkerApp localhost worker2 &
    java -cp /app.jar worker.WorkerApp localhost worker3 &

    # Wait briefly to ensure workers are registered
    sleep 5

    # Start the master service in the background
    java -cp /app.jar master.MasterApp localhost worker1,worker2,worker3 &

    # Wait briefly to ensure master is ready
    sleep 5

    # Start the client service in the foreground
    java -cp /app.jar client.ClientApp localhost
  "

if [ $? -ne 0 ]; then
  echo "Error: Failed to start container"
  exit 1
fi

echo "Container $CONTAINER_NAME is running"
