package worker.backend;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import me.myself.grpc.GrpcServiceGrpc;
import me.myself.grpc.Task;
import me.myself.grpc.TaskRequest;

public class WorkerThread implements Runnable{

    GrpcServiceGrpc.GrpcServiceBlockingStub stub;

    public WorkerThread( GrpcServiceGrpc.GrpcServiceBlockingStub stub ) {
        this.stub = stub;
    }

    @Override
    public void run() {
        for(int i = 0; i < 1; i++)
            this.getTask();
    }

    long idLast = 0;
    public void getTask() {

        Task t;
        try {
//            idLast = t.getId();
            idLast = System.nanoTime();

            t = stub.requestTask( TaskRequest.newBuilder().setIdCompletedTask(idLast).build() );

            doWork( t );

        } catch (StatusRuntimeException e) {
            System.err.println( e.getStatus() );

            if (e.getStatus() == Status.RESOURCE_EXHAUSTED) {
                try {
                    this.wait(2 * 1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void doWork(Task t) {

        System.out.println("Ask: " + idLast + ", Got: " + t.getId() + (idLast == t.getId() ? "" : " - Different!!!!!"));
    }

}
