package worker

import shared._

import java.rmi.server.UnicastRemoteObject

class WorkerImpl(name:String) extends UnicastRemoteObject with WorkerRemote{
    override def executeTask(task: Task): Result = {
        println(s"Worker $name processing $task")
        task match {
            case WordCountTask(text) =>
                val wordCount = text.split("\\s+").count(_.nonEmpty)
                WordCountResult(wordCount)
            case _ => throw new IllegalArgumentException("Unsupported Task Type")
        }
    }
}