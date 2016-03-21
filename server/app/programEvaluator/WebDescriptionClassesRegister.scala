package programEvaluator

import leon.purescala.Definitions.CaseClassDef
import leon.webDSL.webDescription.{TestWebElement2, WebPage}
import scala.reflect.runtime.universe

/**
  * Created by dupriez on 3/21/16.
  *
  * All the concrete classes from the leon.webDSL.webDescription package must be registered in "fullNameToConstructorMap"
  */
object WebDescriptionClassesRegister {

  val fullNameToConstructorMap : Map[String, universe.MethodMirror] = Map(
    ("leon.webDSL.webDescription.WebPage", getReflectConstructor[WebPage]),
    ("leon.webDSL.webDescription.TestWebElement2", getReflectConstructor[TestWebElement2]),
    ("leon.collection.Cons", getReflectConstructor[leon.collection.Cons[_]]),
    ("leon.collection.Nil", getReflectConstructor[leon.collection.Nil[_]])
  )

  private def getReflectConstructor[T: universe.TypeTag] = {
    val mirror = universe.runtimeMirror(getClass.getClassLoader)
    val classs = universe.typeOf[T].typeSymbol.asClass
    val classMirror = mirror.reflectClass(classs)
    val constructor = universe.typeOf[T].decl(universe.termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)
    constructorMirror
  }
}
