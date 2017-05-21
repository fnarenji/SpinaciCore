package wow.realm.entities

import java.sql.{PreparedStatement, ResultSet}

import scalikejdbc._
import wow.common.codecs._
import scodec.Codec
import scodec.codecs._

/**
  * Represents a unique Guid attributed to an entity.
  */
case class Guid(id: Guid.Id, guidType: GuidType.Value) extends Ordered[Guid] {
  override def compare(that: Guid): Int = this.id - that.id

  require(id >= 0)
}

object Guid {
  type Id = Int

  val codec: Codec[Guid] = (
    ("guid" | uintL(24)) ::
      constantE(0)(uintL(24)) ::
      ("guidType" | Codec[GuidType.Value])
    ).as[Guid]

  val packedCodec: Codec[Guid] = zeroPacked(8, codec)

  /**
    * Constructs a type binder for Guid
    * @param guidType guid type
    * @return guid type binder
    */
  def typeBinder(guidType: GuidType.Value): TypeBinder[Guid] = new TypeBinder[Guid] {
    override def apply(rs: ResultSet, columnIndex: Int): Guid = Guid(rs.getInt(columnIndex), guidType)

    override def apply(rs: ResultSet, columnLabel: String): Guid = Guid(rs.getInt(columnLabel), guidType)
  }

  implicit val parameterBinder = new ParameterBinderFactory[Guid] {
    override def apply(guid: Guid): ParameterBinderWithValue = {
      new ParameterBinderWithValue  {
        override def value: Any = guid

        override def apply(stmt: PreparedStatement, idx: Int): Unit = stmt.setInt(idx, guid.id)
      }
    }
  }
}
