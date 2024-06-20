package com.vonage.hdapqa1698.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vonage.hdapqa1698.helpers.Constants;
import com.vonage.hdapqa1698.pojo.IcebergUserLoginData;

import java.io.IOException;
import java.time.LocalDateTime;

public class IcebergUserLoginDataDeserializer extends JsonDeserializer<IcebergUserLoginData> {

    @Override
    public IcebergUserLoginData deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return new IcebergUserLoginData(
                LocalDateTime.parse(node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.TIME).asText(), Constants.DATE_TIME_FORMATTER),
                node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.ACCOUNT_ID).asLong(),
                node.get(Constants.UNDERSCORE + Constants.SOURCE + Constants.PERIOD + Constants.LOG).asText().split(Constants.SEARCH_PARAM)[1]
        );
    }
}
