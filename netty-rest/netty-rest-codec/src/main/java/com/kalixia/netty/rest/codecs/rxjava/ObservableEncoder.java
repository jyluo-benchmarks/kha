package com.kalixia.netty.rest.codecs.rxjava;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kalixia.netty.rest.ObservableApiResponse;
import io.netty.buffer.BufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import rx.Observable;
import rx.Observer;
import rx.Subscription;

import javax.inject.Inject;

/**
 * Encoder transforming RxJava's {@link Observable} into many HTTP objects.
 * <p>
 * This encoder transforms {@link Observable}s into many {@link io.netty.handler.codec.http.HttpMessage}s,
 * hence sends chunked HTTP responses.
 */
@ChannelHandler.Sharable
public class ObservableEncoder extends MessageToMessageEncoder<ObservableApiResponse<?>> {

    @Inject
    ObjectMapper objectMapper;

    private static final ByteBuf LIST_BEGIN = Unpooled.wrappedBuffer("[".getBytes());
    private static final ByteBuf LIST_END   = Unpooled.wrappedBuffer("]".getBytes());
    private static final ByteBuf LIST_ITEM_SEPARATOR = Unpooled.wrappedBuffer(",".getBytes());

    @Override
    @SuppressWarnings("unchecked")
    protected void encode(final ChannelHandlerContext ctx, final ObservableApiResponse<?> apiResponse, final MessageBuf<Object> out)
            throws Exception {
        // TODO: figure out which HTTP status to send!
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, apiResponse.status());
        HttpHeaders.setTransferEncodingChunked(response);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, apiResponse.contentType());
        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        // insert request ID header
        if (apiResponse.id() != null) {
            response.headers().set("X-Api-Request-ID", apiResponse.id().toString());
        }
        out.add(response);

        out.add(new DefaultHttpContent(LIST_BEGIN));

        Subscription subscription = apiResponse.observable().subscribe(new Observer() {
            private boolean first = true;

            @Override
            public void onNext(Object args) {
                // TODO: figure out how to process the result as content
                try {
                    byte[] content = objectMapper.writeValueAsBytes(args);
                    ByteBuf buffer = null;
                    if (first) {
                        buffer = Unpooled.wrappedBuffer(content);
                        first = false;
                    } else {
                        buffer = Unpooled.wrappedBuffer(LIST_ITEM_SEPARATOR, Unpooled.wrappedBuffer(content));
                    }
                    DefaultHttpContent chunk = new DefaultHttpContent(buffer);
                    out.add(chunk);
                } catch (JsonProcessingException e) {
                    ctx.fireExceptionCaught(e);
                }
            }

            @Override
            public void onCompleted() {
                DefaultLastHttpContent lastChunk = new DefaultLastHttpContent(LIST_END);
                out.add(lastChunk);
            }

            @Override
            public void onError(Exception e) {
                ctx.fireExceptionCaught(e);
            }

        });

        subscription.unsubscribe();

    }

}
