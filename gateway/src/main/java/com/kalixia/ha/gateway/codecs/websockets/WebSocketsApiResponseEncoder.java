package com.kalixia.ha.gateway.codecs.websockets;

import com.kalixia.ha.gateway.ApiResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketsApiResponseEncoder extends MessageToMessageEncoder<ApiResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketsApiResponseEncoder.class);

    public WebSocketsApiResponseEncoder() {
        super(ApiResponse.class);
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, ApiResponse msg) throws Exception {
        return new TextWebSocketFrame(msg.content());
    }

}