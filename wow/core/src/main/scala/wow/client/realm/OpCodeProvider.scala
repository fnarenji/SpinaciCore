package wow.client.realm

import wow.auth.protocol.OpCodes

import scala.language.implicitConversions

/**
  * Class used to indicite which opcode a payload is associated to
  *
  * @param opCode opcode
  * @tparam A payload type
  */
case class OpCodeProvider[A](opCode: OpCodes.Value)

object OpCodeProvider {
  /**
    * Implicit cast from opcode to OpCodeProvider
    *
    * @param opCode opCode
    * @tparam A payload type
    * @return opcode provider for payload type A
    */
  implicit def opCodeToProvider[A](opCode: OpCodes.Value): OpCodeProvider[A] = new OpCodeProvider[A](opCode)
}
