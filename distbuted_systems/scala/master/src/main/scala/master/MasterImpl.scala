package master

import shared.{MasterRemote, Result, Task, WorkerRemote}

import java.rmi.server.UnicastRemoteObject

class MasterImpl(workers: Seq[WorkerRemote]) extends UnicastRemoteObject with MasterRemote {
    private var currentWorker = 0

    override def submitTask(task: Task): Result = {
        if (workers.isEmpty) throw new IllegalStateException("No workers available")
        val worker = workers(currentWorker)
        currentWorker = (currentWorker + 1) % workers.size
        println(s"Master assigning task $task to worker ${currentWorker % workers.size}")
        worker.executeTask(task)
    }
}
