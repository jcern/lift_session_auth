package code
package model

import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http.SessionVar

/**
 * The singleton that has methods for accessing the database
 */
object User extends User {
  object roles extends SessionVar[Box[List[UserRole]]](Empty)


  //Add any roles necessary
  def login(n:String, pw:String) = {
    if(n == "test" && pw == "test") {
      roles(Box !! List(UserRole("user")))
      true
    } else
      false
  }
}

case class UserRole(val name:String)

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
case class User() {

}

