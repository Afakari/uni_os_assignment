# RMI Distributed System

This project demonstrates the use of Java RMI (Remote Method Invocation) in a distributed system using a master-worker model. A client submits tasks (e.g., word count) to a master, which delegates them to workers for processing. The system is designed to provide a high-level overview of RMI-based distributed computing.

## Prerequisites

- **Scala**: 2.13.14
- **SBT**: 1.9.9
- **SBT Assembly Plugin**: 2.1.5
- **Java**: OpenJDK 17
- **Docker**: Installed and running

## Project Structure

- **shared**: Contains common RMI interfaces and task definitions (e.g., `WordCountTask`, `WordCountResult`).
- **worker**: Implements worker nodes that process tasks.
- **master**: Implements the master node that coordinates tasks.
- **client**: Submits tasks (e.g., word count) to the master.
- **root**: Aggregates all modules and builds a single JAR.

## Setup and Running

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd rmi-distributed-system
   ```

2. **Build the Project**:
   Run the following command to clean, compile, and assemble the project into a single JAR (`project-assembly-1.0.jar`):
   ```bash
   sbt clean compile assembly
   ```

3. **Run the Application**:
   Ensure Docker is installed and running. Execute the provided Bash script to build the Docker image and run all services (registry, workers, master, client) in a single container:
   ```bash
   chmod +x start.sh
   ./start.sh
   ```

## How It Works

- The **client** submits tasks (e.g., word count for text like "Hello world") to the **master** via RMI.
- The **master** distributes tasks to available **workers**.
- **Workers** process tasks and return results to the **master**, which relays them to the **client**.
- The RMI registry facilitates communication between components.

## Notes

- The project uses a single Docker container for simplicity, running all services (`shared.CustomRegistry`, `worker.WorkerApp`, `master.MasterApp`, `client.ClientApp`).
- The `registry.policy` file must be in the project root for RMI security settings.
- The client submits over 100 word count tasks to demonstrate task distribution.

This project is intentionally simple to illustrate RMI concepts in a distributed master-worker system.