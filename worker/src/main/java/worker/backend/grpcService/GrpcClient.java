package worker.backend.grpcService;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import me.myself.grpc.Task;
import me.myself.grpc.TaskRequest;
import worker.Params;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import me.myself.grpc.GrpcServiceGrpc.GrpcServiceBlockingStub;
import static me.myself.grpc.GrpcServiceGrpc.newBlockingStub;


/**
 * V1 uses gRPC Blocking Stubs, which block the thread while waiting for the response.
 * Yet, the behavior observed is that sharing the same stub between multiple threads does not block the stub resources
 * to other threads. So, it can run in paralel.
 * Still, Non-Blocking Stubs are recommended - https://grpc.io/docs/guides/performance/
 *
 */
public class GrpcClient {

    private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool( Params.numWorkerThreads );


    public GrpcClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress( host, port ).usePlaintext().build();

        GrpcServiceBlockingStub stub = newBlockingStub(channel);

        for(int i = 0; i < pool.getMaximumPoolSize(); i++)
            pool.execute( new WorkerThread(stub) );
    }


    static class WorkerThread implements Runnable{

        GrpcServiceBlockingStub stub;
        long idLastTask = 0;

        public WorkerThread( GrpcServiceBlockingStub stub ) {
            this.stub = stub;
        }

        @Override
        public void run() {
            while( true )
                try{
                    doWork(
                            stub.requestTask( TaskRequest.newBuilder().setIdCompletedTask(idLastTask).build() )
                    );
                } catch (StatusRuntimeException e) {
                    System.err.println( e.getStatus() );

                    if (e.getStatus() == Status.RESOURCE_EXHAUSTED) break;

                    try {
                        this.wait(2 * 1000);
                    } catch (InterruptedException ignored) {}
                }
        }


        public void doWork(Task t) {

            idLastTask = t.getId();
            System.out.println("Got: " + t.getId());
        }

    }
}
