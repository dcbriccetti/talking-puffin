package bootstrap.liftweb

import net.liftweb.sitemap.{Menu, SiteMap}
import net.liftweb.sitemap.Loc._
import net.liftweb.widgets.flot._
import net.liftweb.common.{Loggable, Full}
import net.liftweb.widgets.tablesorter.TableSorter
import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import org.talkingpuffin.snippet.Auth
import net.liftweb.util.Props
import org.talkingpuffin.model.User
import net.liftweb.http.{S, ResourceServer, LiftRules}

class Boot extends Loggable {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
        new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
          Props.get("db.url") openOr
            "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
          Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    LiftRules.addToPackages("org.talkingpuffin")
    Schemifier.schemify(true, Schemifier.infoF _, User)

    val LoggedIn    = If(()     => loggedIn_?, "Not logged in")
    val NotLoggedIn = Unless(() => loggedIn_?, "Logged in")

    LiftRules.setSiteMap(SiteMap(List(
      Menu("Home") / "index" >> Hidden,
      Menu("Log In") / "login" >> Hidden,
      Menu("Analyze") / "analyze" >> LoggedIn,
      Menu("People") / "people" >> LoggedIn)
      : _*))

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
    LiftRules.ajaxPostTimeout = 60000

    LiftRules.early.append {_.setCharacterEncoding("UTF-8")}

    LiftRules.loggedInTest = Full(() => loggedIn_?)

    ResourceServer.allow {
      case "css" :: _ => true
      case "js" :: _ => true
      case "media" :: _ => true
    }

    Flot.init
    TableSorter.init

    S.addAround(DB.buildLoanWrapper)
  }

  private def loggedIn_? = {
    val in = Auth.loggedIn.get
    logger.info("Logged in? " + in)
    in
  }
}