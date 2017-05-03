package ensiwow.realm.protocol

import scala.language.implicitConversions

/**
  * Class used to indicite which opcode a payload is associated to
  * @param opCode opcode
  * @tparam T payload type
  */
case class OpCodeProvider[T](opCode: OpCodes.Value)

object OpCodeProvider {
  /**
    * Implicit cast from opcode to OpCodeProvider
    * @param opCode opCode
    * @tparam T payload type
    * @return opcode provider for payload type T
    */
  implicit def opCodeToProvider[T](opCode: OpCodes.Value): OpCodeProvider[T] = new OpCodeProvider[T](opCode)
}

