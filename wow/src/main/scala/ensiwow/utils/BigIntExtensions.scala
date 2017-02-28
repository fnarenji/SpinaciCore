package ensiwow.utils

import ensiwow.auth.protocol.codecs.fixedUBigIntL
import scodec.bits.BitVector

/**
  * Additional functions for unsigned BigInt little endian serialization/deserialization
  */
object BigIntExtensions {
  implicit class RichBigInt(val value: BigInt) extends AnyVal {
    private def requiredSize = (value.bitLength + 7) / 8

    def toUnsignedLBytes(): Array[Byte] = {
      toUnsignedLBytes(requiredSize)
    }

    def toUnsignedLBytes(fixedSize: Int): Array[Byte] = {
      if (requiredSize > fixedSize) {
        throw new IllegalArgumentException("Value is bigger than can be encoded in specified size")
      }

      if (value < 0) {
        throw new IllegalArgumentException("Value < 0 can not be encoded by unsigned encoder")
      }

      val byteArray = value.toByteArray

      val trimmed = byteArray.drop(byteArray.length - requiredSize)

      val paddingSize = fixedSize - trimmed.length
      val padding = Array.fill[Byte](paddingSize.toInt)(0)

      val padded = padding ++ trimmed

      val reversed = padded.reverse

      reversed
    }
  }

  implicit class RichBigIntCompanion(value: BigInt.type) {
    def fromUnsignedLBytes(bytes: Array[Byte]): BigInt = {
      val reversed = bytes.reverse

      val signed = Array[Byte](0) ++ reversed

      BigInt(signed)
    }
  }

}
