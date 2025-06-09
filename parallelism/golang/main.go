package main

import (
	"encoding/csv"
	"fmt"
	"log"
	"math/rand/v2"
	"os"
	"runtime"
	"sort"
	"strconv"
	"sync"
	"time"
)

type Task struct {
	id   int
	Data []int
}

func worker(wg *sync.WaitGroup, tasks <-chan Task, id int, results chan<- Task) {
	defer wg.Done()
	log.Printf("worker %d starting", id)
	for task := range tasks {
		log.Printf("worker %d received task %d", id, task.id)
		sort.Ints(task.Data)
		results <- task
	}
	log.Printf("worker %d done", id)
}

func merge(left, right []int) []int {
	result := make([]int, 0, len(left)+len(right))
	l, r := 0, 0
	for l < len(left) && r < len(right) {
		if left[l] <= right[r] {
			result = append(result, left[l])
			l++
		} else {
			result = append(result, right[r])
			r++
		}
	}
	result = append(result, left[l:]...)
	result = append(result, right[r:]...)
	return result
}

func CreateLargeCsv(filePath string, numRecords int) {
	file, err := os.Create(filePath)
	if err != nil {
		log.Fatalf("Failed to create file: %s", err)
	}
	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	for i := 0; i < numRecords; i++ {
		record := []string{strconv.Itoa(rand.IntN(numRecords * 10))}
		if err := writer.Write(record); err != nil {
			log.Fatalf("Failed to write to file: %s", err)
		}
	}
}

func readCsv(filePath string, numRecords int) ([]int, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, fmt.Errorf("failed to open CSV: %w", err)
	}
	defer file.Close()

	reader := csv.NewReader(file)
	allNumbers := make([]int, 0, numRecords)
	for {
		record, err := reader.Read()
		if err != nil {
			if err.Error() == "EOF" {
				break
			}
			return nil, fmt.Errorf("failed to read CSV: %w", err)
		}
		if len(record) == 0 {
			continue
		}
		n, err := strconv.Atoi(record[0])
		if err != nil {
			return nil, fmt.Errorf("invalid number in CSV: %w", err)
		}
		allNumbers = append(allNumbers, n)
	}
	return allNumbers, nil
}

func main() {
	const (
		csvFile    = "large_file.csv"
		numRecords = 10_000_000
	)

	numWorkers := runtime.NumCPU()
	chunkSize := numRecords / numWorkers

	log.SetOutput(os.Stdout)
	fmt.Println("Creating a large CSV file...")
	CreateLargeCsv(csvFile, numRecords)
	fmt.Println("CSV Created.")

	fmt.Println("Reading CSV file...")
	allNumbers, err := readCsv(csvFile, numRecords)
	if err != nil {
		log.Fatalf("Error reading CSV: %s", err)
	}
	fmt.Printf("Read %d numbers from CSV.\n", len(allNumbers))

	fmt.Printf("\nStarting Parallel Sort with %d workers...\n", numWorkers)
	startTime := time.Now()

	tasks := make(chan Task, numWorkers)
	results := make(chan Task, len(allNumbers)/chunkSize+1)
	var wg sync.WaitGroup

	for i := 1; i <= numWorkers; i++ {
		wg.Add(1)
		go worker(&wg, tasks, i, results)
	}

	go func() {
		defer close(tasks)
		taskId := 1
		for i := 0; i < len(allNumbers); i += chunkSize {
			end := i + chunkSize
			if end > len(allNumbers) {
				end = len(allNumbers)
			}
			taskData := make([]int, end-i)
			copy(taskData, allNumbers[i:end])
			tasks <- Task{id: taskId, Data: taskData}
			taskId++
		}
	}()

	go func() {
		wg.Wait()
		close(results)
	}()

	var allSortedChunks [][]int
	for res := range results {
		allSortedChunks = append(allSortedChunks, res.Data)
	}

	for len(allSortedChunks) > 1 {
		var newChunks [][]int
		for i := 0; i < len(allSortedChunks); i += 2 {
			if i+1 < len(allSortedChunks) {
				merged := merge(allSortedChunks[i], allSortedChunks[i+1])
				newChunks = append(newChunks, merged)
			} else {
				newChunks = append(newChunks, allSortedChunks[i])
			}
		}
		allSortedChunks = newChunks
	}

	parallelDuration := time.Since(startTime)
	fmt.Println("\nParallel sort finished.")
	fmt.Printf("    Total time taken: %s\n", parallelDuration)
	fmt.Printf("    Used %d Cpu threads, chunk size = %d\n", numWorkers, chunkSize)

	sequentialNumbers := make([]int, len(allNumbers))
	copy(sequentialNumbers, allNumbers)
	seqStartTime := time.Now()
	sort.Ints(sequentialNumbers)
	seqDuration := time.Since(seqStartTime)

	fmt.Println("Sequential sort finished!")
	fmt.Printf("   Time taken: %s\n", seqDuration)

	fmt.Println("\nPerformance Comparison:")
	if parallelDuration < seqDuration {
		fmt.Printf("Parallel sort was faster by %s (%.2f%% faster).\n\n",
			seqDuration-parallelDuration,
			(float64(seqDuration-parallelDuration)/float64(seqDuration))*100)
	} else {
		fmt.Printf("Sequential sort was faster by %s (%.2f%% faster).\n\n",
			parallelDuration-seqDuration,
			(float64(parallelDuration-seqDuration)/float64(parallelDuration))*100)
	}
	os.Remove(csvFile)
}
