package shared
import boopickle.Default._
import boopickle.PicklerHelper
import leon.webDSL.webDescription._

object Picklers extends PicklerHelper {

  /**
    * Link to the documentation of boopickle: https://github.com/ochrons/boopickle
    *   The most interesting parts for the purpose of manually declaring the picklers for the elements of the
    *   webDescription package are the sections "Class hierarchies", "Recursive composite types" and "Complex type hierarchies"
    */
  implicit val webElementPickler = compositePickler[WebElement]
  webElementPickler.addConcreteType[Div]
  webElementPickler.addConcreteType[Header]
  webElementPickler.addConcreteType[Paragraph]

  private val consCode = 1
  private val nilCode = 2

  implicit def listPickler[T: P]: P[leon.collection.List[T]] = new P[leon.collection.List[T]] {
    override def pickle(obj: leon.collection.List[T])(implicit state: PickleState): Unit = {
      // check if this List has been pickled already
      import scala.{Some, None}
      state.identityRefFor(obj) match {
        case Some(idx) =>
          // encode index as negative "length"
          state.enc.writeInt(-idx)
        case None =>
          obj match {
            case leon.collection.Cons(h, t) =>
              state.enc.writeInt(consCode)
              write[T](h)
              write[leon.collection.List[T]](t)
            case leon.collection.Nil() =>
              state.enc.writeInt(nilCode)
          }
          state.addIdentityRef(obj)
      }
    }
    override def unpickle(implicit state: UnpickleState): leon.collection.List[T] = {
      import scala.Right
      state.dec.readIntCode match {
        case Right(`consCode`) =>
          leon.collection.Cons(read[T], read[leon.collection.List[T]])
        case Right(`nilCode`) =>
          leon.collection.Nil()
        case Right(idx) if idx < 0 =>
          state.identityFor[leon.collection.List[T]](-idx)
        case _ =>
          throw new IllegalArgumentException("Invalid coding for leon.collection.List type")
      }
    }
  }

}
