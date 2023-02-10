package master.backend.grpcService;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import master.backend.TaskFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrpcServer {

    private final Server server;
    private final TaskFactory factory;
    private final RequestHandler handler;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    /**
     *
     * @param port Network port to serve
     */
    public GrpcServer(int port) throws InterruptedException {

        factory = new TaskFactory();
        handler = new RequestHandler( factory );

        server = ServerBuilder.forPort( port )
                .addService( handler )
                .build();
    }

    public void startServer() throws IOException {
        server.start();
    }

    public void shutdown() throws InterruptedException {
        factory.shutdown();
        handler.getLatch().await();

        server.shutdown();
        server.awaitTermination();
        isRunning.set(false);
    }

    public void shutdownNow() throws InterruptedException {
        factory.shutdown();

        server.shutdownNow();
        isRunning.set(false);
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
