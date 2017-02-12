package ensiwow.auth

<<<<<<< HEAD
import ensiwow.auth.protocol.ClientLogonChallenge
import ensiwow.auth.protocol.ClientLogonProof
=======
import ensiwow.auth.protocol.packets.ClientLogonChallenge
>>>>>>> 8c542e46edf4a9e257e5176dff697f063a848e20
import scodec._
import scodec.bits._

/**
  * Created by sknz on 1/31/17.
  */
object Application {
  def main(args: Array[String]): Unit = {
    val bits = hex"00082800576F57000303053430363878006E69570053556E653C0000007F0000010A534B4E5A424F474F5353".bits

    val decode = Codec[ClientLogonChallenge].decode(bits)

    println(decode.require.value)
  }

}
