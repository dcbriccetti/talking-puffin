package org.talkingpuffin.web

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FiltersServlet extends HttpServlet {
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) = {
    resp.setContentType("text/plain")
    resp.getWriter().println(
"""Just joined a twibe. Visit http\://twibes\.com/.*
just joined a video chat at http\://tinychat\.com.*
""")
  }
}