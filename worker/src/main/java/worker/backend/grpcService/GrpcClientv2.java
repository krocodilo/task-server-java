package worker.backend.grpcService;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import me.myself.grpc.Task;
import me.myself.grpc.TaskRequest;
import worker.Params;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import me.myself.grpc.GrpcServiceGrpc.GrpcServiceFutureStub;
import static me.myself.grpc.GrpcServiceGrpc.newFutureStub;


/**
 * V2 uses gRPC Non-Blocking Stubs, as recommended - https://grpc.io/docs/guides/performance/
 */
public class GrpcClientv2 {

    private final GrpcServiceFutureStub stub;
    private final LinkedBlockingQueue<Long> completedTaskIds = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool( Params.numWorkerThreads );
    private final AtomicBoolean shutdown = new AtomicBoolean(false);


    public GrpcClientv2(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress( host, port ).usePlaintext().build();

        stub = newFutureStub(channel);
    }

    public void beginWork() {

        for(int i = 0; i < pool.getMaximumPoolSize(); i++)
            // Initial requests to the server
            requestTask( 0 );

        while ( ! shutdown.get() ){
            // Make new request for each completed task
            try{
                Long idCompleted = completedTaskIds.poll(1, TimeUnit.SECONDS); // TODO must use take()
                requestTask(
                        idCompleted == null? 0 : idCompleted
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Execute gRPC request
     * @param idCompletedTask
     */
    public void requestTask(long idCompletedTask) {
        ListenableFuture<Task> ft = stub.requestTask(
                TaskRequest.newBuilder().setIdCompletedTask( idCompletedTask ).build()
        );
        // Once a response is received, it will execute FutureTask()
        Futures.addCallback( ft, new FutureTask(), pool );
    }


    /**
     *
     */
    class FutureTask implements FutureCallback<Task> {

        @Override
        public void onSuccess(Task task) {
            try{
                doWork( task );
                completedTaskIds.add( task.getId() );  // If work throws exception, task will not be marked as completed
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Throwable e) {
            try {
                Status s = ((StatusRuntimeException) e).getStatus();
                if( s == Status.RESOURCE_EXHAUSTED ){
                    //todo
                } else if( s == Status.UNKNOWN ){
                    //todo
                }
            } catch (ClassCastException ignore) {
                // If not a StatusRuntimeException
                e.printStackTrace();
            }
            completedTaskIds.add( 0L );
        }

        public void doWork(Task t) {

            if( t.getId() % 10000 == 0)
                System.out.println("Got: " + t.getId());
        }
    }

    public void shutdown() {
        shutdown.set(true);
    }
}
