package wow.common

import com.typesafe.config.{ConfigObject, ConfigValue, ConfigValueType}
import pureconfig.ConfigReader
import pureconfig.ConvertHelpers._
import pureconfig.error.{ConfigReaderFailures, ConfigValueLocation, WrongType}
import shapeless.Lazy

import scala.collection.JavaConverters._

/**
  * Configuration parsing helpers etc
  */
package object config {
  /**
    * Copy paste from pureconfig source code, as was private
    */
  private def improveFailures[Z](result: Either[ConfigReaderFailures, Z], keyStr: String, location: Option[ConfigValueLocation]): Either[ConfigReaderFailures, Z] =
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
          fail(WrongType(other.valueType, Set(ConfigValueType.OBJECT), ConfigValueLocation(other), None))
      }
    }
  }

}
