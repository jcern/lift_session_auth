package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import js.jquery.JQueryArtifacts
import sitemap._
import Loc._
import mapper._

import code.model._
import net.liftmodules.JQueryModule
import net.liftweb.http.auth.{userRoles, AuthRole, HttpBasicAuthentication}
import net.liftweb.http.rest.RestHelper
import org.apache.commons.codec.binary.Base64


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    def authenticates: PartialFunction[Req, Box[UserRole]] = {
      case Req(List("login", "protected"), _, _) => Full(UserRole("user"))
      case Req(List("login", "protected2"), _, _) => Full(UserRole("user"))
    }

    def myGuard:PartialFunction[Req, Unit] = {
      case r if(authorizedResponse(r).isEmpty) =>
    }

    def authorizedResponse(req: Req) = {
      if(authenticates.isDefinedAt(req)) {
        User.roles.get match {
          case Full(rol) =>
            for{
              required <- authenticates.apply(req) if(!rol.contains(required))
            } yield {
              ForbiddenResponse("unauthorized")
            }
          case _ => Full(ForbiddenResponse("unauthorized"))
        }
      } else
        Empty
    }

    LiftRules.earlyResponse prepend {
      (req: Req) => authorizedResponse(req)
    }

    LiftRules.dispatch.append(myGuard guard new RestHelper{
      serve {
        case Req("login" :: "protected" :: Nil, _, _) =>
          InMemoryResponse("Protected Content - You are logged in".getBytes(), Nil, Nil, 200)
        case Req("login" :: username :: password :: Nil, _, _) =>
          User.login(username, password)
          InMemoryResponse("Success".getBytes(), Nil, Nil, 200)
      }
    })

    // where to search snippet
    LiftRules.addToPackages("code")

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Protected2") / "login" / "protected2",
      Menu.i("Home") / "index")

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemap)

    //Init the jQuery module, see http://liftweb.net/jquery for more information.
    LiftRules.jsArtifacts = JQueryArtifacts
    JQueryModule.InitParam.JQuery=JQueryModule.JQuery172
    JQueryModule.init()

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))


    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    


  }
}
