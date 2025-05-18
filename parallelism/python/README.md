# Parallel Computation Script Documentation

## Overview

This Python script demonstrates parallel processing using the `multiprocessing` module to perform computationally
intensive tasks across multiple CPU cores. It showcases CPU-bounded costly computations and compares parallel execution
with sequential execution. The script logs task progress to a file for better showcase.

## Prerequisites

- **Python**: Version 3.8 or higher.

## Setup Instructions

### 1. Install Dependencies

The script requires the `numpy` and `tqdm` Python packages. These are listed in a `requirements.txt` file.

#### Install Dependencies

Open a terminal and run:

```bash
pip install -r requirements.txt
```

This installs the required packages:

- `numpy`: For mathematical computations.
- `tqdm`: For displaying progress bars.

## Running the Script

Execute the script using:

   ```bash
   python main.py
   ```

**Expected Output**:
The script will:

- Display the number of jobs (10) and workers ( half of CPU cores).
- Show a progress bar for parallel task execution.
- Print the results and total time for parallel execution.
- Run the same tasks sequentially with a progress bar.
- Print the sequential results and time for comparison.
- Save task logs to `multiprocessing.log`.

Example output:
```
Dispatching 10 jobs across 8 workers.
Job Progress: 100%|██████████| 10/10 [00:01<00:00, 6.51it/s]
Sequential Jobs:   0%| | 0/10 [00:00<?, ?it/s]
All jobs done!
Final outcomes: [result list]
Total time taken: 1.57 seconds

    Sequential Execution for Comparison:
    Sequential Jobs: 100%|██████████| 10/10 [00:05<00:00,  1.69it/s]
    Sequential outcomes: [result list]
    Sequential time taken: 5.91 seconds
    ```

## Notes

- The script is optimized for multi-core CPUs. On a single-core system, parallel execution may not be faster than
  sequential.
- The log file (`multiprocessing.log`) grows with each run. Delete it periodically if not needed.
- For heavy computations, ensure your system has enough memory and CPU resources.

