package shared

// mainly oop stuff, added for flexibility
trait Result extends Serializable


// Any result functino
case class WordCountResult(count: Int) extends Result