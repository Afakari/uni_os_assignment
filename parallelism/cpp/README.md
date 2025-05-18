## Overview

This C++ script demonstrates parallel processing using `std::async` to perform CPU-intensive computations across
multiple cores. It computes `sin(data) * cos(data) + data^2` for 10 tasks, compares parallel and sequential execution.

## Prerequisites

- **C++ Compiler**: g++, clang++, or MSVC (C++11 or higher).
- **OS**: Windows, macOS, or Linux.
- **Threads**: Compiler must support `<thread>` (e.g., g++ 4.8+, MSVC 2015+).

## Setup Instructions

### 1. Compile

Open a terminal and compile with threading support:

- **Linux/macOS**:
  ```bash
  g++ -std=c++17 main.cpp -o main -pthread
  ```

- **Windows (MinGW)**:
  ```bash
  g++ -std=c++17 main.cpp -o main.exe -pthread
  ```

- **Windows (MSVC)**:
  ```bash
  cl /EHsc /std:c++17 main.cpp
  ```

### 2. Run

Execute the program:

- **Linux/macOS**:
  ```bash
  ./main
  ```

- **Windows**:
  ```bash
  main.exe
  ```

**Expected Output**:

- Shows 10 tasks on detected cores.
- Displays a progress bar (`[==========]`) for parallel and sequential runs.
- Prints results and times for both.

Example:

  ```
  Running 10 tasks on 8 cores.
  Processing:
  [===] Done!
  
  Results: [result list]
  Time: 1.50 s
  
  Sequential run:
  [==========] Done!
  Sequential results: [result list]
  Sequential time: 5.67 s
  ```

## Notes

- Optimized for multi-core CPUs; parallel run is faster on multi-core systems.
- No external libraries; uses C++ Standard Library.
- Ensure `-pthread` for g++/clang++ or proper MSVC setup.
- For heavy tasks, check CPU/memory availability.