package na.datapipe.transformer.transform

import com.google.gson.{JsonObject, JsonParser}

/**
 * @author nader albert
 * @since  3/12/2015.
 */
trait JsonParserTest {
  protected def extractJsonObjectFromText(jsonText: String, jsonExtract: String) = {
    val jsonParser = new JsonParser

    jsonParser.parse(jsonText).getAsJsonObject.getAsJsonObject(jsonExtract)
  }

  protected def parse(jsonText: String): JsonObject = {
    val jsonParser = new JsonParser
    jsonParser.parse(jsonText).getAsJsonObject
  }

}
