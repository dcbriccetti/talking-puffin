package org.talkingpuffin.web

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FiltersServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = {
    resp.setContentType("text/plain")
    resp.getWriter().println(
"""Just joined a twibe\. Visit http\://twibes\.com/.*
just joined a video chat at http\://tinychat\.com.*
I just ousted .* as the mayor of .* on @foursquare!.*
ran .* on .* at .* with a pace of .*
Just completed a .* run with @runkeeper, check it out.*
just voted .* on .* vote too .*
Just added myself to the .*wefollow.*
.*add a \#twibbon to your avatar now.*
Hey, I just added you to my Mafia family\. You should accept my invitation! :\) Click here: .*
I favorited a YouTube video.*
""")
  }
}