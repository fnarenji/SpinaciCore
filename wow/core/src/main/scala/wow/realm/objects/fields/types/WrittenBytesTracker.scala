package wow.realm.objects.fields.types

import scodec.bits.BitVector

class WrittenBytesTracker(size: Long) {
  private var bitMask = BitVector.empty

  def set(l: Long) = {
    require(bitMask.size + l <= size)
    bitMask = bitMask ++ BitVector.high(l)
  }

  def notSet(l: Long) = {
    require(bitMask.size + l <= size)
    bitMask = bitMask ++ BitVector.low(l)
  }

  def finish(): BitVector = bitMask.padLeft(size)
}

