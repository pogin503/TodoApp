package daos

import javax.inject.Inject

import models.Todo
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.driver.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class TodoDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Todos = TableQuery[TodosTable]

  /** 全検索 */
  def all(): Future[Seq[Todo]] = db.run(Todos.result)

  /** 登録 */
  def insert(content: String): Future[Unit] = {
    val todos = Todos returning Todos.map(_.id) into ((todo, id) => todo.copy(id = id)) += Todo(0, content)
    db.run(todos.transactionally).map(_ => ())
  }

  /** 更新 */
  def update(id: Long, content: String): Future[Unit] = {
    db.run(Todos.filter(_.id === id).map(_.content).update(content)).map(_ => ())
  }

  /** 削除 */
  def delete(id: Long): Future[Unit] = {
    db.run(Todos.filter(_.id === id).delete).map(_ => ())
  }

  /** マッピング */
  private class TodosTable(tag: Tag) extends Table[Todo](tag, "TODO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def content = column[String]("CONTENT")
    def * = (id, content) <> (Todo.tupled, Todo.unapply _)
  }

}