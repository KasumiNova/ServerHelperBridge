package github.kasuminova.serverhelper.network;

import github.kasuminova.network.codec.CompressedObjectDecoder;
import github.kasuminova.network.codec.CompressedObjectEncoder;
import github.kasuminova.serverhelper.ServerHelperBridge;
import github.kasuminova.serverhelper.network.handler.MainHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final BridgeClient cl;

    public ClientInitializer(BridgeClient cl) {
        this.cl = cl;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addFirst("CompressedObjectEncoder", new CompressedObjectEncoder());
        pipeline.addFirst("CompressedObjectDecoder", new CompressedObjectDecoder(ServerHelperBridge.class.getClassLoader()));

        pipeline.addLast("MainHandler", new MainHandler(cl));
    }
}
