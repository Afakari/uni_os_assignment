package shared

// mainly oop stuff, added for flexibility
trait Task extends Serializable

// Any task function
case class WordCountTask(text: String) extends Task