package com.forketyfork.hellslick

import slick.driver.H2Driver.api._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

object SlickDemo extends App {

  // ------- Инициализация

  // подключение к БД
  val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver="org.h2.Driver")

  // таблица людей
  class Persons(tag: Tag) extends Table[(Int, String)](tag, "persons") {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name)
  }
  val persons = TableQuery[Persons]

  // таблица телефонов, связанных с людьми *-1
  class Phones(tag: Tag) extends Table[(Int, Int, String)](tag, "phones") {
    def id = column[Int]("id", O.PrimaryKey)
    def personId = column[Int]("person_id")
    def number = column[String]("number")
    def * = (id, personId, number)
    def person = foreignKey("person_fk", personId, persons)(_.id)
  }
  val phones = TableQuery[Phones]

  // Накатываем на таблицу схему БД
  val schemaCreateAction = (persons.schema ++ phones.schema).create
  Await.result(db.run(schemaCreateAction), 1 second)


  // ------- Запросы к БД

  // запрос с использованием монадического джойна
  val monadicInnerJoin = for {
    ph <- phones
    pe <- persons if ph.personId === pe.id
  } yield (pe.name, ph.number)

  // делаем из запроса DBIO-действие
  val action1 = monadicInnerJoin.result

  // делаем DBIO-действие из какой-то произвольной функции
  val action2 = DBIO.successful {
    println("Делаем что-то между запросами в транзакции...")
  }

  // ещё парочка DBIO-действий...
  val action3 = persons += (1, "Grace")
  val action4 = phones += (1, 1, "+1 (800) FUC-KYOU")

  // делаем композитное действие из всех четырёх действий
  val compositeAction = for {
    result <- action1
    _ <- action2
    personCount <- action3
    phoneCount <- action4
  } yield personCount + phoneCount

  // заворачиваем композитное действие в транзакцию и делаем из него футуру
  val actionFuture = db.run(compositeAction.transactionally)

  // выполняем футуру и выводим её результат — количество вставленных записей
  val databaseFuture = for {
    i <- actionFuture
    _ <- Future {
      println(s"Вставлено записей: $i")
    }
  } yield ()

  Await.result(databaseFuture, 1 second)

}
