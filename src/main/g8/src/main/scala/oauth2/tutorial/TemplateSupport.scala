package oauth2.tutorial

import org.fusesource.scalate.TemplateEngine

trait TemplateSupport {

  private val engine = new TemplateEngine

  val webroot: String

  def render(template: String, params: Map[String, String]) = {
    engine.layout(
      s"\$webroot/\$template.mustache",
      params
    )
  }
}
