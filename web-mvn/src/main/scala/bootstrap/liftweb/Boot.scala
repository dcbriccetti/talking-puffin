package bootstrap.liftweb

import net.liftweb.sitemap.{Menu, SiteMap}
import net.liftweb.sitemap.Loc._
import net.liftweb.widgets.flot._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.http.{ResourceServer, LiftRules}
import net.liftweb.widgets.tablesorter.TableSorter
import org.talkingpuffin.snippet.Auth

class Boot extends Loggable {
  def boot {
    LiftRules.addToPackages("org.talkingpuffin")

    val LoggedIn    = If(()     => loggedIn_?, "Not logged in")
    val NotLoggedIn = Unless(() => loggedIn_?, "Logged in")

    LiftRules.setSiteMap(SiteMap(List(
      Menu("Home") / "index" >> Hidden,
      Menu("Log In") / "login" >> Hidden,
      Menu("Statuses") / "statuses" >> LoggedIn,
      Menu("People") / "people" >> LoggedIn)
      : _*))

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append {_.setCharacterEncoding("UTF-8")}

    LiftRules.loggedInTest = Full(() => loggedIn_?)

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
      case "media" :: _ => true
    }

    Flot.init
    TableSorter.init

  }

  private def loggedIn_? = {
    val in = Auth.loggedIn.get
    logger.info("Logged in? " + in)
    in
  }
}