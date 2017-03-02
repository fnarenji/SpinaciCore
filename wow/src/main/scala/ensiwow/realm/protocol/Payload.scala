package ensiwow.realm.protocol

/**
  * Represents a class that is a payload
  */
trait Payload {
  def opCode: OpCodes.Value
}


