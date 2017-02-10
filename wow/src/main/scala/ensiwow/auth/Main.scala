package ensiwow.auth

import ensiwow.auth.protocol.ClientLogonChallenge
import ensiwow.auth.protocol.ClientLogonProof
import scodec._
import scodec.bits._

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  def main(args: Array[String]): Unit = {
    val bits = hex"00082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353".bits
    val strLogonProof = hex"0134EBE82023D7398C8497037625C33E91EBF8FD906392F8531C41314FC13D1935DC6BF1ACF8827B93A2B1E44992BF2F779313213EFC95E6AEC9FC8C5C7C9CB8AC058E2DE83DAC40D30000".bits

    //val decode = Codec[ClientLogonChallenge].decode(bits)
    val decode = Codec[ClientLogonProof].decode(strLogonProof)

    println(decode.require.value)
  }

}
