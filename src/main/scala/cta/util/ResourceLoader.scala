package cta.util

import scala.util.Using
import scala.io.Source

object ResourceLoader {

  def resourceAsString(resourcePath: String): String = {
    Using.resource(getClass.getClassLoader.getResourceAsStream(resourcePath)) { stream =>
      Source.fromInputStream(stream).mkString
    }
  }
}
