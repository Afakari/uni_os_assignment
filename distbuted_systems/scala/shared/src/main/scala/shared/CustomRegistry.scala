package shared

import sun.rmi.registry.RegistryImpl

object CustomRegistry {
    def main(args: Array[String]): Unit = {
        println("--- CustomRegistry Debug Output ---")
        println(s"Java Version: ${System.getProperty("java.version")}")
        println(s"Attempted -Djava.rmi.server.hostname: ${System.getProperty("java.rmi.server.hostname")}")
        println(s"Attempted -Djava.security.manager: ${System.getProperty("java.security.manager")}") // Shows if -Djava.security.manager was passed as a string
        println(s"Attempted -Djava.security.policy: ${System.getProperty("java.security.policy")}")
        println("-----------------------------------")

        val currentSecurityManager = System.getSecurityManager()
        if (currentSecurityManager == null) {
            println("CRITICAL: System.getSecurityManager() is NULL at the start of CustomRegistry.")
            println("This means -Djava.security.manager did NOT effectively enable the Security Manager, or it was unset.")
        } else {
            println(s"SUCCESS: System.getSecurityManager() is ACTIVE. Class: ${currentSecurityManager.getClass.getName}")
            // You can try a benign operation that a restrictive policy (if AllPermission wasn't loaded) might block
            try {
                System.getProperty("user.home") // Example: Read a system property
                println("Successfully read 'user.home' system property (indicates SM is allowing some operations).")
            } catch {
                case e: SecurityException =>
                    println(s"WARNING: SecurityException while reading 'user.home': ${e.getMessage}")
                    println("This might indicate the policy file isn't granting permissions as expected, or wasn't found.")
            }
        }
        println("-----------------------------------")

        try {
            // The RegistryImpl constructor will export the registry object.
            // It will internally check System.getSecurityManager().
            val registry = new RegistryImpl(1099)
            println(s"Custom RMI Registry (sun.rmi.registry.RegistryImpl) instance created successfully.")
            println(s"Registry should be listening on port 1099. Waiting for connections...")
        } catch {
            case e: Exception =>
                println(s"FATAL ERROR creating/exporting RegistryImpl: ${e.getClass.getName} - ${e.getMessage}")
                e.printStackTrace()
        }

        // Keep the main thread alive
        try {
            Thread.currentThread().join()
        } catch {
            case ie: InterruptedException => println("Main thread interrupted.")
        }
    }
}