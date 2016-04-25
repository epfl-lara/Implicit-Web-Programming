package programEvaluator

import leon.purescala.Expressions.{AsInstanceOf, _}
import stringModification.StringModificationProcessor.TupleSelectOrCaseClassSelect

/**
  * Created by dupriez on 4/25/16.
  */
object TupleSelectAndCaseClassSelectRemover {
  def removeTopLevelTupleSelectsAndCaseClassSelects(expr: Expr): Expr = {
    // Just to have a shorter name to use inside
    def recurse(expr1: Expr): Expr = {
      expr1 match {
        case TupleSelect(Tuple(args), i) =>
          recurse(args(i - 1))
        case TupleSelect(arg, i) =>
          recurse(TupleSelect(recurse(arg), i))
        case CaseClassSelector(cct, CaseClass(ct, args), id) =>
          recurse(args(cct.classDef.selectorID2Index(id)))
        case CaseClassSelector(cct, inExpr, id) =>
          recurse(CaseClassSelector(cct, recurse(inExpr), id))
        case AsInstanceOf(expr2, classType) =>
          recurse(expr2)
        case _ => expr1
      }
    }
    recurse(expr)
  }
}
