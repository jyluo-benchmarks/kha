package com.kalixia.netty.rest.codecs.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class ByteBufSerializer extends JsonSerializer<ByteBuf> {
    @Override
    public void serialize(ByteBuf byteBuf, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jsonGenerator.writeString(new String(byteBuf.array(), CharsetUtil.UTF_8));
    }

    @Override
    public Class<ByteBuf> handledType() {
        return ByteBuf.class;
    }
}
