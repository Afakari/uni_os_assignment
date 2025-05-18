import logging
import multiprocessing
import time

import numpy as np
from tqdm import tqdm

# logger setup in a file instead of terminal
logging.basicConfig(
    filename="multiprocessing.log",
    level=logging.INFO,
    format="%(asctime)s - %(processName)s - %(message)s"
)


def task_processor(work_item):
    task_id, data = work_item
    try:
        logging.info(f"Task {task_id} started")
        computation = 0
        # random math -> very costly process demonstration
        for _ in range(5 * 10 ** 5):
            computation += np.sin(data) * np.cos(data) + data * data
        logging.info(f"Task {task_id} complete, result: {computation:.4f}")
        return task_id, computation
    except Exception as e:
        logging.error(f"Task {task_id} failed: {str(e)}")
        raise


if __name__ == "__main__":
    try:
        # fix compatibility issues
        multiprocessing.set_start_method('spawn', force=True)
    except RuntimeError:
        pass

    start = time.time()
    input_data = list(range(1, 11))
    job_list = [(i + 1, item) for i, item in enumerate(input_data)]
    core_count = min(multiprocessing.cpu_count() // 2, len(job_list))

    print(f"Dispatching {len(job_list)} jobs across {core_count} workers.")

    final_results = [None] * len(job_list)
    # mapping futures
    try:
        with multiprocessing.Pool(processes=core_count) as worker_pool:
            # used tqdm for the progression bar / live info
            pbar = tqdm(total=len(job_list), desc="Job Progress", dynamic_ncols=True)
            for task_id, computation in worker_pool.imap_unordered(task_processor, job_list):
                final_results[task_id - 1] = computation
                pbar.update()
            pbar.close()
    except Exception as e:
        print(f"Error during multiprocessing: {str(e)}")
        logging.error(f"Multiprocessing error: {str(e)}")

    finish = time.time()

    print("\nAll jobs done!")
    print(f"Final outcomes: {final_results}")
    print(f"Total time taken: {finish - start:.2f} seconds")

    print("\nSequential Execution for Comparison:")
    sequential_start = time.time()
    sequential_outcomes = []
    # gather the futures
    for job in tqdm(job_list, desc="Sequential Jobs", dynamic_ncols=True):
        task_id, result = task_processor(job)
        sequential_outcomes.append(result)
    sequential_finish = time.time()
    print(f"Sequential outcomes: {sequential_outcomes}")
    print(f"Sequential time taken: {sequential_finish - sequential_start:.2f} seconds")
