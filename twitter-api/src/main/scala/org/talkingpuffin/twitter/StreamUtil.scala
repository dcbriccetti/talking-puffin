package org.talkingpuffin.twitter

import java.io.{InputStreamReader, BufferedInputStream, BufferedReader, OutputStreamWriter, InputStream}

object StreamUtil {
  def streamToString(stream: InputStream): String = {
    val buf = new StringBuilder
    val br = new BufferedReader(new InputStreamReader(stream))
    var line: String = null
    var eof = false
    while (! eof) {
      line = br.readLine()
      if (line == null) eof = true else 
      buf.append(line).append("\n")
    }
    buf.toString
  }  
}

