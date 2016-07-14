package com.forketyfork.hellslick

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object FutureDemo extends App {

  // первая футура формирует и возвращает строку
  def getFuture1 = Future {
    "1337"
  }

  // вторая футура из строки делает число
  def getFuture2(string: String) = Future {
    string.toInt
  }

  // комбинированная футура, созданная с использованием for-включения
  val composedFuture = for {
    result1 <- getFuture1
    result2 <- getFuture2(result1)
  } yield result2

  println(Await.result(composedFuture, 1 second))


  // комбинированная футура, созданная с использованием flatMap и map
  val composedFuture2 = getFuture1.flatMap { string =>
    getFuture2(string).map { int =>
      int
    }
  }

  println(Await.result(composedFuture2, 1 second))

}
