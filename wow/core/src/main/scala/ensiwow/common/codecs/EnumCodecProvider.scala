package ensiwow.common.codecs

import scodec._

import scala.language.implicitConversions

/**
  * Enumeration trait providing a codec for a Numeric type.
  */
trait EnumCodecProvider[T] extends Enumeration {
  /**
    * Codec numeric tag
    */
  protected val valueCodecTag: NumericCodecTag[T]

  implicit lazy val codec: Codec[Value] = {
    val valueCodec = valueCodecTag.valueCodec
    val numeric = valueCodecTag.numeric

    val valuesMap = values.toList.map(e => {
      e -> numeric.fromInt(e.id)
    }) toMap

    scodec.codecs.mappedEnum[Value, T](valueCodec, valuesMap)
  }
}

/**
  * Captures a numeric for use with a codec for an Enumeration
  *
  * @param valueCodec value codec
  * @param numeric numeric for codec type
  * @tparam T codec type
  */
case class NumericCodecTag[T](valueCodec: Codec[T], numeric: Numeric[T])

object NumericCodecTag {
  /**
    * Converts a codec into its numeric codec tag
    *
    * @param valueCodec value codec to tag
    * @tparam T type of codec
    * @return numeric codec tag
    */
  implicit def codecToNumericCodeTag[T](valueCodec: Codec[T])
    (implicit numeric: Numeric[T]): NumericCodecTag[T] = NumericCodecTag[T](valueCodec, numeric)
}
