import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._

@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  "Batch API" should {

    "return aggregate response" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/batch?foo=%2Ffoo&bar=%2Fbar")
      browser.pageSource must beEqualTo("""{"foo":{"message":"Hi, this is Foo"},"bar":{"message":"Hi, this is Bar"}}""")
    }
  }
}
