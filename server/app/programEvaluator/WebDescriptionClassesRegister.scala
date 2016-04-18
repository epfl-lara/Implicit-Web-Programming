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

  //The boolean indicates whether it extends WebElement
  val fullNameToConstructorMap : Map[String, (universe.MethodMirror, Boolean)] = Map(
    ("leon.webDSL.webDescription.WebPage", (getReflectConstructor[WebPage], false)),
    ("leon.webDSL.webDescription.Div", (getReflectConstructor[Div], true)),
    ("leon.webDSL.webDescription.Header", (getReflectConstructor[Header], true)),
    ("leon.webDSL.webDescription.Paragraph", (getReflectConstructor[Paragraph], true)),
    ("leon.webDSL.webDescription.HLOne", (getReflectConstructor[HLOne], false)),
    ("leon.webDSL.webDescription.HLTwo", (getReflectConstructor[HLTwo], false)),
    ("leon.webDSL.webDescription.HLThree", (getReflectConstructor[HLThree], false)),
    ("leon.webDSL.webDescription.HLFour", (getReflectConstructor[HLFour], false)),
    ("leon.webDSL.webDescription.HLFive", (getReflectConstructor[HLFive], false)),
    ("leon.webDSL.webDescription.HLSix", (getReflectConstructor[HLSix], false)),
    ("leon.collection.Cons", (getReflectConstructor[leon.collection.Cons[_]], false)),
    ("leon.collection.Nil", (getReflectConstructor[leon.collection.Nil[_]], false))//,
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