package ensiwow.utils

import ensiwow.auth.protocol.codecs.fixedUBigIntL
import scodec.bits.BitVector

/**
  * Additional functions for unsigned BigInt little endian serialization/deserialization
  */
object BigIntExtensions {
  implicit class RichBigInt(value: BigInt) {
    def toNetworkBytes(size: Int) : Array[Byte] = {
      fixedUBigIntL(size).encode(value).require.toByteArray
    }
    def toNetworkBytes() : Array[Byte] = {
      // Round up to closest multiple of 8 (excl. sign bit) and deduce size in bytes
      val size = (value.bitLength + 7) / 8
      toNetworkBytes(size)
    }
  }
  implicit class RichBigIntCompanion(value: BigInt.type) {
    def fromNetworkBytes(bytes: Array[Byte]) = fixedUBigIntL(bytes.length).decode(BitVector(bytes)).require.value
  }
}
