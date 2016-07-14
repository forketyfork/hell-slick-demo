package com.forketyfork.hellslick

object ListDemo extends App {

  // списки
  val people = List("Воронин", "Гейгер", "Убуката")
  val positions = List("мусорщик", "следователь", "редактор")


  // декартово произведение списков с использованием for-включения:
  val peoplePositions = for {
    person <- people
    position <- positions
  } yield s"$person, $position"

  println(peoplePositions)


  // декартово произведение списков прямым вызовом flatMap и map:
  val peoplePositions2 = people.flatMap {person =>
    positions.map { position =>
      s"$person, $position"
    }
  }

  println(peoplePositions2)

}
