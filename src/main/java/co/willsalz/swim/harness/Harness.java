package co.willsalz.swim.harness;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import co.willsalz.swim.agent.GossipAgent;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Harness {

    private static final Logger logger = LoggerFactory.getLogger("harness");

    public static void main(String[] args) throws InterruptedException {
        // Setup netty logging
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
        final GossipAgent agent3 = new GossipAgent(
            Arrays.asList(
                new InetSocketAddress("localhost", agent2.getPort()),
                new InetSocketAddress("localhost", agent1.getPort()))
        );

        // Start both agents
        logger.info("Starting agents...");
        final Future<?> agent1Future = executor.submit(agent1);
        final Future<?> agent2Future = executor.submit(agent2);
        final Future<?> agent3Future = executor.submit(agent3);

        // Send a handlePing from agent2
        logger.info("Starting PingReqs...");
        agent3.pingReq(new InetSocketAddress("localhost", agent1.getPort()));
        Thread.sleep(100);
        agent2.pingReq(new InetSocketAddress("localhost", agent1.getPort()));
        Thread.sleep(100);
        agent1.pingReq(new InetSocketAddress("localhost", agent3.getPort()));

        // Wait a bit then shutdown.
        Thread.sleep(1000);
        logger.info("Shutting down...");
        agent1Future.cancel(true);
        agent2Future.cancel(true);
        agent3Future.cancel(true);
        executor.shutdown();
    }
}
