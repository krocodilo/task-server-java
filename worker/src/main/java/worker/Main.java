package worker;

import worker.backend.grpcService.GrpcClient;
import worker.backend.grpcService.GrpcClientv2;


public class Main {

    public static void main(String[] args) {

        if( Params.numWorkerThreads < 1 )
            throw new RuntimeException("numWorkerThreads parameter must be greater than zero");

//        GrpcClient client = new GrpcClient("localhost", 9090);
        GrpcClientv2 client = new GrpcClientv2("localhost", 9090);
        client.beginWork();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                //Shutdown signal handler
        {
            //this will also be called when System.exit() is used!
            client.shutdown();
//            System.out.println("Waiting " + workerTerminationTimeout + " seconds (max) for threads to finish.");
//            try {
//                if( pool.awaitTermination(workerTerminationTimeout, TimeUnit.SECONDS) )
//                    return;  //success waiting
//            } catch (InterruptedException ignored) {}
//            System.out.println("\nWARNING: unable to wait for existing tasks to finish.");
            System.out.println("\nClient has terminated.\n\n\n");
        }));
        System.out.println("\nClient has terminated.\n\n\n");
    }
}
