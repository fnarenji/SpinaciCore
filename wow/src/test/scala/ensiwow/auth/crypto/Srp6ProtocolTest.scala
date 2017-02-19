package ensiwow.auth.crypto

import org.scalatest.{FlatSpec, Matchers}

import scodec.bits._

import scala.collection.mutable

/**
  * Tests the validity of the SRP6 implementation by comparing produced values against expected values
  */
class Srp6ProtocolTest extends FlatSpec with Matchers {
  behavior of "Srp6ProtocolTest"

  val Username = "t"
  val Password = "t"

  val FixedServerPrivateKey = BigInt("5698844817725982222235344496777980655886111343")
  val FixedUnk3 = BigInt("194942597757323744367948666173918899059")
  val FixedSalt = BigInt("78823503796391676434485569088161368409945032487538050771151147647624579312285")

  val FixedClientKey = BigInt("12788190605557339135743505648605577622246116826573644792769669732542065193474")
  val FixedClientProof = BigInt("206698099087563234717514472059746285009952185143")

  val ExpectedVerifier = BigInt("22075422366936545515674385768650057420632007409200282980604073279761866516199")
  val ExpectedIdentity = Srp6Identity(FixedSalt, ExpectedVerifier)

  val ExpectedServerKey = BigInt("34065449131815595092332791003415184197068868016788977698739449184781844147149")
  val ExpectedChallenge = Srp6Challenge(FixedServerPrivateKey, ExpectedServerKey)

  val ExpectedServerProof = hex"07AC9CAE83F8612E80A5A9FFB4A09CAB1D19BE51"
  val ExpectedSharedKey = BigInt("300378318409522422353872320923135826702415933338008804808601593020685099547324428108406102214443")
  val ExpectedProof = Srp6Proof(ExpectedServerProof, ExpectedSharedKey)

  class FixedRandomBigInt(fixedRandomValues: BigInt*) extends RandomBigInt {
    private val valuesQueue = mutable.Queue[BigInt](fixedRandomValues: _*)

    override def next(sizeInBits: Int) = valuesQueue.dequeue()
  }

  it should "computeSaltAndVerifier" in {
    val fixedRandom = new FixedRandomBigInt(FixedSalt)
    val srp6 = new Srp6Protocol(fixedRandom)

    val verifier = srp6.computeSaltAndVerifier(Username, Password)

    verifier shouldEqual ExpectedIdentity
  }

  it should "computeChallenge" in {
    val fixedRandom = new FixedRandomBigInt(FixedServerPrivateKey)
    val srp6 = new Srp6Protocol(fixedRandom)

    val challenge = srp6.computeChallenge(ExpectedIdentity)

    challenge shouldEqual ExpectedChallenge
  }

  it should "verify" in {
    val random = new FixedRandomBigInt()
    val srp6 = new Srp6Protocol(random)

    srp6.verify(Username, FixedClientKey, FixedClientProof, ExpectedIdentity, ExpectedChallenge) match {
      case Some(srp6Proof) =>
        srp6Proof shouldEqual ExpectedProof
      case None =>
        fail("Invalid credential verification")
    }
  }
}
