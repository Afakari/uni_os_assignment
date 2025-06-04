package worker

import java.rmi.registry.LocateRegistry
import java.rmi.{ConnectException, RemoteException}

object WorkerApp {
    def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            println("usage WorkerApp <registryHost> <workerName>")
            System.exit(1)
        }
        val registryHost = args(0)
        val workerName = args(1)
        val worker = new WorkerImpl(workerName)

        var attempts = 0
        val maxAttempts = 5
        var registry: java.rmi.registry.Registry = null
        while (attempts < maxAttempts && registry == null) {
            try {
                registry = LocateRegistry.getRegistry(registryHost, 1099)
                registry.bind(workerName, worker)
                println(s"Worker $workerName bound to registry at $registryHost")
            }
            catch {
                case e: ConnectException =>
                    attempts += 1
                    if (attempts < maxAttempts) Thread.sleep(2000)
                case e: RemoteException =>
                    println(s"RemoteException: ${e.getMessage}")
                    System.exit(1)
            }
            if (registry == null) {
                println("Failed to connect to registry")
                System.exit(1)
            }
        }
        // idle
        Thread.currentThread().join()
    }

}
