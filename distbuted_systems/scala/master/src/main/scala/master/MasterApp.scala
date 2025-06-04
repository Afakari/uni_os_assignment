package master

import shared.WorkerRemote

import java.rmi.registry.LocateRegistry
import java.rmi.{ConnectException, RemoteException}

object MasterApp {
    def main(args: Array[String]): Unit = {
        if (args.length < 2) {
            println("usage: MasterApp <registryHost> <workerNames>")
            System.exit(1)
        }
        val registryHost = args(0)
        val workerNames = args(1).split(',')
        var attempts = 0
        val maxAttempts = 5
        var registry: java.rmi.registry.Registry = null
        while (attempts < maxAttempts && registry == null) {
            try {
                registry = LocateRegistry.getRegistry(registryHost, 1099)
                val workers = workerNames.map { name =>
                    registry.lookup(name).asInstanceOf[WorkerRemote]
                }
                val master = new MasterImpl(workers)
                registry.bind("master", master)
                println(s"Master bound to registry at $registryHost with workers: ${workerNames.mkString(", ")}")
            } catch {
                case e: ConnectException =>
                    attempts += 1
                    println(s"Failed to connect to registry (attempt $attempts/$maxAttempts): ${e.getMessage}")
                    if (attempts < maxAttempts) Thread.sleep(2000)
                case e: RemoteException =>
                    println(s"RemoteException: ${e.getMessage}")
                    System.exit(1)
            }
        }
        if (registry == null) {
            println(s"Failed to connect to registry after $maxAttempts attempts")
            System.exit(1)
        } // idle
        Thread.currentThread().join()
    }

}
