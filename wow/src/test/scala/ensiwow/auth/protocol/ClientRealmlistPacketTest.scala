package ensiwow.auth.protocol

import ensiwow.auth.protocol.packets.ClientRealmlistPacket

import org.scalatest.{FlatSpec, Matchers}

import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scodec.{Codec, DecodeResult}

import scala.reflect.runtime.universe.TypeTag

/**
  * Created by yanncolina on 15/02/17.
  * This class was <strike>copied</strike> inspired by the AuthPacketTest class, maybe we should reorganize the tests
  * of this file in order to avoid duplicates.
  */

class RealmlistPacketTest[T](bytes: ByteVector, reference: T)
                            (implicit val typeTag: TypeTag[T],
                             implicit val codec: Codec[T]) extends FlatSpec with Matchers {

  private val packetBits = bytes.bits

  typeTag.tpe.toString must "be fully and correctly hydrated" in {
    val attempt = codec.decode(packetBits)
    attempt match {
      case Failure(err) => fail(err.toString())
      case Successful(DecodeResult(packet, BitVector.empty)) =>
        packet shouldEqual reference

        val encode = codec.encode(reference)
        encode match {
          case Successful(bits) => bits shouldEqual packetBits
          case Failure(err) => fail(err.toString())
        }
      case Successful(DecodeResult(packet, remainder)) => fail(s"non empty remainder: $packet / $remainder")
    }
  }
}

class ClientRealmlistPacketTest extends RealmlistPacketTest[ClientRealmlistPacket](
  hex"1000000000",
  ClientRealmlistPacket()
)

/*
class ServerRealmlistPacketTest extends RealmlistPacketTest[ServerRealmlistPacket](
  hex"1029000000000001000100025472696E697479003132372E302E302E313A3830383500000000000101011000",
  ServerRealmlistPacket(
    packetSize = 0x29,
    nbrRealms = 1,
    realms = Vector(
      RealmlistPacket(
        realmType = 1,
        lock = 0,
        flags = 0x2,
        name = "Trinity",
        ip = "127.0.0.1:8085",
        populationLevel = 0.0f,
        characterCount = 1,
        timezone = 1,
        id = 1,
        versionInfo = VersionInfo.SupportedVersionInfo)
      )
  )
)
*/
