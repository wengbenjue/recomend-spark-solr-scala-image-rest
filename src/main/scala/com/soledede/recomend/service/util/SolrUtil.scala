package com.soledede.recomend.service.util

/**
  * Created by soledede on 2015/12/22.
  */
object SolrUtil {

 def escapeQueryChars(query: String): String = {
    val sb = new StringBuilder()
    for (i <- 0 to query.length() - 1) {
      val c = query.charAt(i);
      if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '('
        || c == ')' || c == ':' || c == '^' || c == '[' || c == ']'
        || c == '{' || c == '}' || c == '~'
        || c == '*' || c == '?' || c == '|' || c == '&' || c == ';'
        || c == '/') {
        sb.append('\\')
      }
      sb.append(c)
    }

    var q = sb.toString();
    q = q.replaceAll("“", "\"")
    q = q.replaceAll("”", "\"")
    q = q.toUpperCase()
    q
  }



}
