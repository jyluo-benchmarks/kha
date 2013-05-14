package com.kalixia.ha.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalixia.ha.gateway.codecs.rest.RESTCodec;
import com.kalixia.ha.gateway.codecs.websockets.WebSocketsApiRequestDecoder;
import com.kalixia.ha.gateway.codecs.websockets.WebSocketsApiResponseEncoder;
import com.kalixia.ha.gateway.codecs.websockets.WebSocketsServerProtocolUpdater;
import io.netty.buffer.BufUtil;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.InetSocketAddress;

/**
 * Alters the pipeline in order to cope with both HTTP REST API handlers and WebSockets handlers.
 */
public class ApiProtocolSwitcher extends MessageToMessageDecoder<FullHttpRequest> {
    private final ObjectMapper objectMapper;
    private static final ChannelHandler webSocketsServerProtocolUpdater = new WebSocketsServerProtocolUpdater();
    private static final ChannelHandler webSocketsApiResponseEncoder = new WebSocketsApiResponseEncoder();
    private static final ChannelHandler restCodec = new RESTCodec();
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiProtocolSwitcher.class);

    public ApiProtocolSwitcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, MessageBuf<Object> out) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();

        if (msg.getUri().equals("/websocket")) {
            LOGGER.debug("Switching to WebSockets pipeline...");
            pipeline.addBefore("api-request-logger", "ws-protocol-updater", webSocketsServerProtocolUpdater);
            pipeline.addAfter("ws-protocol-updater", "api-response-encoder-ws", webSocketsApiResponseEncoder);
            pipeline.addAfter("api-response-encoder-ws", "api-request-decoder-ws", new WebSocketsApiRequestDecoder(objectMapper));
            pipeline.remove(this);
        } else {
            LOGGER.debug("Switching to REST pipeline...");
//            pipeline.replace(this, "rest-codec", restCodec);
            pipeline.addBefore("api-request-logger", "rest-codec", restCodec);
            pipeline.remove(this);
        }
        out.add(BufUtil.retain(msg));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        MDC.put(MDCLogging.MDC_CLIENT_ADDR, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        MDC.remove(MDCLogging.MDC_CLIENT_ADDR);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Can't setup API pipeline", cause);
        ctx.close();
    }
}
