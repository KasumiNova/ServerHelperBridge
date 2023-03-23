package github.kasuminova.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;

public class CompressedObjectDecoder extends LengthFieldBasedFrameDecoder {
    private final ClassLoader classLoader;

    public CompressedObjectDecoder(ClassLoader classLoader) {
        super(1048576, 0, 4, 0, 4);
        this.classLoader = classLoader;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        byte[] uncompressed = Snappy.uncompress(frame.array());
        try (ObjectDecoderInputStream ois = new ObjectDecoderInputStream(new ByteArrayInputStream(uncompressed), classLoader)) {
            return ois.readObject();
        }
    }
}
