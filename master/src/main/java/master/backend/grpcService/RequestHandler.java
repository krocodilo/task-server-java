package master.backend.grpcService;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import master.Params;
import master.backend.TaskFactory;
import me.myself.grpc.GrpcServiceGrpc;
import me.myself.grpc.Task;
import me.myself.grpc.TaskRequest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RequestHandler extends GrpcServiceGrpc.GrpcServiceImplBase {

    private final TaskFactory factory;
    private final LinkedBlockingQueue<Task> workQueue;
    private final LinkedBlockingQueue<Task> pendingQueue;
    private final CountDownLatch latch = new CountDownLatch(1);

    public RequestHandler(TaskFactory factory) {
        this.factory = factory;
        workQueue = factory.getWorkQueue();
        pendingQueue = new LinkedBlockingQueue<>( Params.maxPendingTasks );
    }

    @Override
    public void requestTask(TaskRequest request, StreamObserver<Task> responseObserver) {

        if( factory.isFinished() ) {
            // If there are no more tasks
            responseObserver.onError(
                    new StatusRuntimeException(Status.RESOURCE_EXHAUSTED)
            );
            removeCompletedTask( request.getIdCompletedTask() );
            if( pendingQueue.size() == 0)
                latch.countDown();
            return;
        }

        try{
            responseObserver.onNext( getNextTask() );   // Save message
            responseObserver.onCompleted(); // Send message right away

        } catch (Exception e) {
            responseObserver.onError(
                    new StatusRuntimeException( Status.UNKNOWN )
            );
        }
        removeCompletedTask( request.getIdCompletedTask() );
    }

    /**
     * Get next task
     * @return Next Task
     * @throws InterruptedException if interrupted while waiting, or timed out.
     */
    private Task getNextTask() throws InterruptedException {
        Task t;
        if( pendingQueue.remainingCapacity() <= 1 )
            // Send the oldest pending task
            t = pendingQueue.poll( Params.requestTimeoutMillis, TimeUnit.MILLISECONDS );
        else
            t = workQueue.poll( Params.requestTimeoutMillis, TimeUnit.MILLISECONDS );

        if(t == null)
            throw new InterruptedException( "Timeout fetching next task." );
        else
            System.out.println("Sent - " + t.getId());
        return t;
    }

    /**
     * Remove Task with given ID from the pendingQueue
     * @param taskId
     */
    private void removeCompletedTask(long taskId) {
        pendingQueue.removeIf( t -> t.getId() == taskId );
    }

    /**
     *
     * @return CountDownLatch
     */
    public CountDownLatch getLatch() {
        return latch;
    }
}
