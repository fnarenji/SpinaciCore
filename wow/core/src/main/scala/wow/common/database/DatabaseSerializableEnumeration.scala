package wow.common.database

import java.sql.ResultSet

import scalikejdbc._

trait EnumerationValueToSqlSyntax[A] {
  def apply(value: A): SQLSyntax
}

/**
  * Makes an enumeration bindable to database
  */
trait DatabaseSerializableEnumeration {
  this: Enumeration =>

  /**
    * The name of the type in the database server
    */
  def typeName: String = SQLSyntaxSupportFactory.camelToSnake(getClass.getSimpleName).trim

  /**
    * Type binder for enum value
    */
  implicit val typeBinder = new TypeBinder[Value] {
    override def apply(rs: ResultSet, columnIndex: Int): Value = DatabaseSerializableEnumeration.this
      .withName(rs.getString(columnIndex))

    override def apply(rs: ResultSet, columnLabel: String): Value = DatabaseSerializableEnumeration.this
      .withName(rs.getString(columnLabel))
  }

  /**
    * Provides a cast from value to SqlSyntax
    */
  implicit val valueToSqlSyntax = new EnumerationValueToSqlSyntax[Value] {
    override def apply(value: Value): scalikejdbc.SQLSyntax = {
      sqls"CAST(${value.toString} AS "
        .append(SQLSyntax.createUnsafely(typeName))
        .append(sqls")")
    }
  }
}
