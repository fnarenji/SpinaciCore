package wow.common

import scalikejdbc.{NamedDB, _}
import scodec.bits.ByteVector

import scala.concurrent.{ExecutionContext, Future, blocking}

package object database {
  /**
    * Provides the names of the current database connections
    */
  var Databases: DatabaseNameProvider = DefaultDatabases

  /**
    * Authentication database connection token
    */
  val AuthDB = NamedDB(Databases.AuthServer)

  /**
    * Runs a blocking database operation in a future
    *
    * @param run operation
    * @param executionContext execution context
    * @tparam A return type
    * @return Future
    */
  def AsyncDB[A](run: => A)(implicit executionContext: ExecutionContext): Future[A] = Future(blocking(run))

  /**
    * Enriches an enumeration value with being transformed to a ParameterBinderWithValue
    *
    * @param self enumeration value
    * @tparam A enumeration type
    */
  implicit class RichValue[A <: Enumeration](val self: A#Value) extends AnyVal {
    def asParameterBinder(implicit ev: EnumerationValueToSqlSyntax[A#Value]): ParameterBinderWithValue =
      ParameterBinderFactory.sqlSyntaxParameterBinderFactory(ev(self))
  }

  /**
    * Constructs a rich wrapped result set
    *
    * @param self result set to build form
    * @return rich wrapped result set
    */
  implicit class RichWrappedResultSet(val self: WrappedResultSet) extends AnyVal {
    def byteVector(columnIndex: Int): ByteVector = ByteVector.view(self.bytes(columnIndex))

    def byteVector(columnLabel: String): ByteVector = ByteVector.view(self.bytes(columnLabel))
  }
}

