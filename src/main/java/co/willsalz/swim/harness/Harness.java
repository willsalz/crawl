package co.willsalz.swim.harness;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import co.willsalz.swim.client.GossipAgent;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;


public class Harness {
    public static void main(String[] args) throws InterruptedException {
        // Setup netty loggign
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

        // Create executor on which to run our agents.
        final ExecutorService executor = Executors.newCachedThreadPool();

        // Create two agents to run concurrently
        final GossipAgent agent1 = new GossipAgent(
            Collections.emptyList()
        );
        final GossipAgent agent2 = new GossipAgent(
            Collections.singletonList(new InetSocketAddress("localhost", agent1.getPort()))
        );

        // Start both agents
        final Future<?> serverFuture = executor.submit(agent1);
        final Future<?> clientFuture = executor.submit(agent2);

        // Send a ping from agent2
        final ChannelFuture f = agent2.ping();
        f.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

        // Wait a bit then shutdown.
        Thread.sleep(100);
        serverFuture.cancel(true);
        clientFuture.cancel(true);
        executor.shutdown();
    }
}
