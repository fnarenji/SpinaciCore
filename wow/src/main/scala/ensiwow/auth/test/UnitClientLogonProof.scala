package ensiwow.auth

import ensiwow.auth.protocol.packets.ClientLogonProof
import scodec._
import scodec.bits._

/**
  * Created by betcheg on 02/11/17.
  */
object UnitClientLogonProof {
  def main(args: Array[String]): Unit = {
    val strLogonProof = hex"0134EBE82023D7398C8497037625C33E91EBF8FD906392F8531C41314FC13D1935DC6BF1ACF8827B93A2B1E44992BF2F779313213EFC95E6AEC9FC8C5C7C9CB8AC058E2DE83DAC40D30000".bits

    val decode = Codec[ClientLogonProof].decode(strLogonProof)

    println(decode.require.value)
  }

}
