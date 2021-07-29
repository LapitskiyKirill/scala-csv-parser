package io

import mapper.Mapper
import validator.Validator

import scala.collection.parallel.CollectionConverters._
import scala.collection.immutable.List
import scala.io.Source
import scala.reflect.runtime._
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

class ParameterizedReader[T: TypeTag](validator: Validator, mapper: Mapper) {
  private val constructor = findConstructor()

  def readFile(fileName: String): List[Option[T]] = {
    val source = Source.fromFile(fileName)
    val lines = source.getLines.drop(1).toList.par.map(readLine)
    source.close()
    lines.toList
  }

  private def readLine(line: String): Option[T] = {
    val parse = Try({
      val iterator = line.split(",").iterator
      val mappedIterator = mapper.map(iterator)
      val finiteObject: Some[T] = Some(
        createInstance(mappedIterator.toSeq)
      )
      if (!validator.validate(finiteObject.value)) {
        throw new Exception
      }
      finiteObject
    })

    parse match {
      case Success(v) =>
        v
      case Failure(_) =>
        Option.empty
    }
  }

  private def findConstructor(): MethodMirror = {
    val tt = typeTag[T]
    currentMirror.reflectClass(tt.tpe.typeSymbol.asClass).reflectConstructor(
      tt.tpe.members.filter(m =>
        m.isMethod && m.asMethod.isConstructor
      ).iterator.toSeq.head.asMethod
    )
  }

  private def createInstance(args: Seq[Any]): T = {
    constructor(args: _*).asInstanceOf[T]
  }
}