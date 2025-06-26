package shared

import sun.rmi.registry.RegistryImpl

object CustomRegistry {
    def main(args: Array[String]): Unit = {
        println("--- CustomRegistry Debug Output ---")
        println(s"Java Version: ${System.getProperty("java.version")}")
        println(s"Attempted -Djava.rmi.server.hostname: ${System.getProperty("java.rmi.server.hostname")}")
        println("-----------------------------------")

        try {
            val registry = new RegistryImpl(1099)
            println(s"Custom RMI Registry (sun.rmi.registry.RegistryImpl) instance created successfully.")
            println(s"Registry should be listening on port 1099. Waiting for connections...")
        } catch {
            case e: Exception =>
                println(s"FATAL ERROR creating/exporting RegistryImpl: ${e.getClass.getName} - ${e.getMessage}")
                e.printStackTrace()
        }

        try {
            Thread.currentThread().join()
        } catch {
            case ie: InterruptedException => println("Main thread interrupted.")
        }
    }
}