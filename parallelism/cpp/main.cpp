#include <iostream>
#include <vector>
#include <future>
#include <cmath>
#include <chrono>
#include <iomanip>
#include <atomic>
#include <thread>

using namespace std;

// Task structure
struct Task {
    int id;
    double data;
};

// Worker function: same computation as Python script
double worker(Task task, atomic<int>& progress) {
    double result = 0.0;
    for (int i = 0; i < 50'000'000; ++i) { // 50M iterations
        result += sin(task.data) * cos(task.data) + task.data * task.data;
    }
    ++progress; // Update progress
    return result;
}

int main() {
    auto start = chrono::high_resolution_clock::now();

    // Detect number of cores
    int num_cores = thread::hardware_concurrency() / 2 ;
    cout << "Running " << 10 << " tasks on " << num_cores << " cores.\n";

    // Create 10 tasks (data: 1 to 10)
    vector<Task> tasks;
    for (int i = 1; i <= 10; ++i) {
        tasks.push_back({i, static_cast<double>(i)});
    }

    atomic<int> progress{0}; // Track completed tasks
    vector<future<double>> futures;

    cout << "Processing:\n[";
    // Launch tasks asynchronously
    for (const auto& task : tasks) {
        futures.push_back(async(launch::async, worker, task, ref(progress)));
    }

    // Simple progress bar
    while (progress < tasks.size()) {
        cout << "=";
        cout.flush();
        this_thread::sleep_for(chrono::milliseconds(500));
    }
    cout << "] Done!\n";

    // Collect results
    vector<double> results(tasks.size());
    for (size_t i = 0; i < futures.size(); ++i) {
        results[i] = futures[i].get();
    }

    auto end = chrono::high_resolution_clock::now();
    double duration = chrono::duration<double>(end - start).count();

    cout << "\nResults: [";
    for (size_t i = 0; i < results.size(); ++i) {
        cout << fixed << setprecision(4) << results[i] << (i == results.size() - 1 ? "" : ", ");
    }
    cout << "]\nTime: " << fixed << setprecision(2) << duration << " s\n";

    // Sequential execution for comparison
    cout << "\nSequential run:\n[";
    progress = 0;
    auto seq_start = chrono::high_resolution_clock::now();
    vector<double> seq_results;
    for (const auto& task : tasks) {
        seq_results.push_back(worker(task, progress));
        cout << "=";
        cout.flush();
    }
    cout << "] Done!\n";

    auto seq_end = chrono::high_resolution_clock::now();
    double seq_duration = chrono::duration<double>(seq_end - seq_start).count();

    cout << "Sequential results: [";
    for (size_t i = 0; i < seq_results.size(); ++i) {
        cout << fixed << setprecision(4) << seq_results[i] << (i == seq_results.size() - 1 ? "" : ", ");
    }
    cout << "]\nSequential time: " << fixed << setprecision(2) << seq_duration << " s\n";

    return 0;
}