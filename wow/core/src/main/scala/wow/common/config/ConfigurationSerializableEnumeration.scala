package wow.common.config

/**
  * Makes an enumeration config convertible
  */
trait ConfigurationSerializableEnumeration {
  this: Enumeration =>

  /**
    * Config converter for Value type
    */
  implicit val valueConfigConverter = wow.common.config.deriveEnumValue(this)

  /**
    * Config converter for ValueSet type
    */
  implicit val valueSetConfigConverter = wow.common.config.deriveEnumValueSet(this)
}



