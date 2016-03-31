package programEvaluator

import leon.purescala.Definitions.CaseClassDef
import leon.webDSL.webDescription._
import scala.reflect.runtime.universe

/**
  * Created by dupriez on 3/21/16.
  *
  * All the concrete classes from the leon.webDSL.webDescription package must be registered in "fullNameToConstructorMap"
  */
object WebDescriptionClassesRegister {

  val fullNameToConstructorMap : Map[String, universe.MethodMirror] = Map(
    ("leon.webDSL.webDescription.WebPage", getReflectConstructor[WebPage]),
    ("leon.webDSL.webDescription.Div", getReflectConstructor[Div]),
    ("leon.webDSL.webDescription.Header", getReflectConstructor[Header]),
    ("leon.webDSL.webDescription.Paragraph", getReflectConstructor[Paragraph]),
    ("leon.webDSL.webDescription.HLOne", getReflectConstructor[HLOne]),
    ("leon.webDSL.webDescription.HLTwo", getReflectConstructor[HLTwo]),
    ("leon.webDSL.webDescription.HLThree", getReflectConstructor[HLThree]),
    ("leon.webDSL.webDescription.HLFour", getReflectConstructor[HLFour]),
    ("leon.webDSL.webDescription.HLFive", getReflectConstructor[HLFive]),
    ("leon.webDSL.webDescription.HLSix", getReflectConstructor[HLSix]),
    ("leon.collection.Cons", getReflectConstructor[leon.collection.Cons[_]]),
    ("leon.collection.Nil", getReflectConstructor[leon.collection.Nil[_]])//,
//    ("leon.lang.Map", getReflectConstructor[leon.lang.Map[_,_]])
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