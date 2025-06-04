package client

import shared.{MasterRemote, WordCountResult, WordCountTask}

import java.rmi.registry.LocateRegistry
import java.rmi.{ConnectException, RemoteException}

object ClientApp {
    def main(args: Array[String]): Unit = {
        if (args.length < 1) {
            println("usage: ClientApp <registryHost>")
            System.exit(1)
        }

        val registryHost = args(0)

        val tasks = generateTasks(101)

        var attempts = 0
        val maxAttempts = 5
        var master: MasterRemote = null
        while (attempts < maxAttempts && master == null) {
            try {
                val registry = LocateRegistry.getRegistry(registryHost, 1099)
                master = registry.lookup("master").asInstanceOf[MasterRemote]

                tasks.zipWithIndex.foreach { case (task, index) =>
                    println(s"Client submitting task ${index + 1}: $task")
                    try {
                        val result = master.submitTask(task)
                        result match {
                            case WordCountResult(count) =>
                                println(s"Client received result for task ${index + 1}: Word count = $count")
                            case _ =>
                                println(s"Unexpected result type for task ${index + 1}")
                        }
                    } catch {
                        case e: RemoteException =>
                            println(s"RemoteException for task ${index + 1}: ${e.getMessage}")
                    }
                }
            } catch {
                case e: ConnectException =>
                    attempts += 1
                    println(s"Failed to connect to registry (attempt $attempts/$maxAttempts): ${e.getMessage}")
                    if (attempts < maxAttempts) Thread.sleep(2000)
                case e: RemoteException =>
                    println(s"RemoteException during lookup: ${e.getMessage}")
                    System.exit(1)
            }
        }
        if (master == null) {
            println(s"Failed to connect to registry after $maxAttempts attempts")
            System.exit(1)
        }
    }

    private def generateTasks(numTasks: Int): List[WordCountTask] = {
        val basePhrases = List(
            "The quick brown fox jumps over the lazy dog",
            "Lorem ipsum dolor sit amet consectetur adipiscing elit",
            "A journey of a thousand miles begins with a single step",
            "To be or not to be that is the question",
            "All that glitters is not gold",
            "The sun sets slowly behind the mountain",
            "A picture is worth a thousand words",
            "When in Rome do as the Romans do",
            "Every cloud has a silver lining",
            "The early bird catches the worm"
        )

        (1 to numTasks).map { i =>
            val baseIndex = (i - 1) % basePhrases.length
            val suffix = s" (Task $i)"
            WordCountTask(basePhrases(baseIndex) + suffix)
        }.toList
    }
}