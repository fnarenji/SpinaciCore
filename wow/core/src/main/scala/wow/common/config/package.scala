package wow.common

import com.typesafe.config.{ConfigList, ConfigObject, ConfigValue, ConfigValueType}
import pureconfig.ConvertHelpers._
import pureconfig.error.{ConfigReaderFailures, ConfigValueLocation, WrongType}
import pureconfig.{ConfigConvert, ConfigReader}
import shapeless.Lazy

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Configuration parsing helpers etc
  */
package object config {
  /**
    * Copy paste from pureconfig source code, as was private
    */
  private def improveFailures[Z](
    result: Either[ConfigReaderFailures, Z],
    keyStr: String,
    location: Option[ConfigValueLocation]): Either[ConfigReaderFailures, Z] =
    result.left.map {
      case ConfigReaderFailures(head, tail) =>
        val headImproved = head.withImprovedContext(keyStr, location)
        val tailImproved = tail.map(_.withImprovedContext(keyStr, location))
        ConfigReaderFailures(headImproved, tailImproved)
    }

  /**
    * Mostly copied from pureconfig source code, but made generic
    */
  implicit def deriveIntMap[B](implicit configConvert: Lazy[ConfigReader[B]]) = new ConfigReader[Map[Int, B]] {

    override def from(config: ConfigValue): Either[ConfigReaderFailures, Map[Int, B]] = {
      config match {
        case co: ConfigObject =>
          val z: Either[ConfigReaderFailures, Map[Int, B]] = Right(Map.empty[Int, B])

          co.asScala.foldLeft(z) {
            case (acc, (key, value)) =>
              combineResults(
                acc,
                improveFailures(configConvert.value.from(value), key, ConfigValueLocation(value))) {
                (map, valueConverted) => map + (key.toInt -> valueConverted)
              }
          }

        case other =>
          fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(other), other.origin().description()))
      }
    }
  }

  /**
    * Enum value config converter
    *
    * @param e enumeration
    * @tparam A enumeration type
    * @return enum value config converter
    */
  implicit def deriveEnumValue[A <: Enumeration](implicit e: A) =
    ConfigConvert.viaNonEmptyString[e.Value](catchReadError(s => e.values.find(_.toString.equalsIgnoreCase(s))
      .getOrElse(throw new NoSuchElementException(s"No value found for '$s'"))), _.toString)

  /**
    * Enum valueset config converter
    *
    * @param e enumeration
    * @tparam A enumeration type
    * @return enum value set config converter
    */
  implicit def deriveEnumValueSet[A <: Enumeration](implicit e: A) = new ConfigReader[e.ValueSet] {
    val valueConvert = deriveEnumValue[A](e)

    override def from(config: ConfigValue): Either[ConfigReaderFailures, e.ValueSet] = {
      config match {
        case co: ConfigList =>
          val baseValue: Either[ConfigReaderFailures, mutable.Builder[e.Value, e.ValueSet]] = Right(e.ValueSet
            .newBuilder)

          // we called all the failures in the list
          co.asScala.foldLeft(baseValue) {
            case (acc, value) =>
              combineResults(acc, valueConvert.from(value)) { case (a, b) => a += e(b.id) }
          }.right.map(_.result())
        case other =>
          fail(WrongType(other.valueType, Set(ConfigValueType.LIST), ConfigValueLocation(other), other.origin().description()))
      }
    }
  }
}
