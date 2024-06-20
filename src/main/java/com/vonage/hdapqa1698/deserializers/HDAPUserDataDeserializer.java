package com.vonage.hdapqa1698.deserializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.pojo.HDAPUserData;
import com.vonage.hdapqa1698.pojo.IcebergUserLoginData;

import java.io.IOException;
import java.time.LocalDateTime;

public class HDAPUserDataDeserializer extends JsonDeserializer<HDAPUserData> {

    @Override
    public HDAPUserData deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        String message = node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.MESSAGE).asText();

        return new HDAPUserData(
                LocalDateTime.parse(node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.TIMESTAMP).asText(), Constants.DATE_TIME_FORMATTER),
                null,
                null,
                null,
                node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.MESSAGE).asText()
        );
    }
}
