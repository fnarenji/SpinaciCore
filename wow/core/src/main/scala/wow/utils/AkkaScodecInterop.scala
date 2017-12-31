package wow.utils

import akka.util.ByteString
import scodec.bits.ByteVector

/**
  * Copied from scodec/scodec-akka
  */
object AkkaScodecInterop {

  implicit class EnrichedByteString(val value: ByteString) extends AnyVal {
    def toByteVector: ByteVector = ByteVector.viewAt((idx: Long) => value(idx.toInt), value.size.toLong)
  }

  implicit class EnrichedByteVector(val value: ByteVector) extends AnyVal {
    def toByteString: ByteString = ByteString.apply(value.toByteBuffer)
  }

}
