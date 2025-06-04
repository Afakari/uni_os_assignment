package shared


import java.rmi.{Remote, RemoteException}

// Client to add tasks to master
// check master for impl
trait MasterRemote extends Remote {
    @throws(classOf[RemoteException])
    def submitTask(task: Task): Result
}

// Delegate tasks to workers
// Check worker for impl
trait WorkerRemote extends Remote {
    @throws(classOf[RemoteException])
    def executeTask(task: Task): Result
}