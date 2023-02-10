package master;

import master.backend.grpcService.GrpcServer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static GrpcServer server;

    public static void main(String[] args) throws IOException, InterruptedException {

        validateParams();

        server = new GrpcServer(9090);

        System.out.println("\nStarting gRPC server...");
        server.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                //Shutdown signal handler
        {
            //this will also be called when System.exit() is used!
            try {
                server.shutdown();
            } catch (InterruptedException e) {
                try {
                    server.shutdownNow();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
//            System.out.println("Waiting " + workerTerminationTimeout + " seconds (max) for threads to finish.");
//            try {
//                if( pool.awaitTermination(workerTerminationTimeout, TimeUnit.SECONDS) )
//                    return;  //success waiting
//            } catch (InterruptedException ignored) {}
//            System.out.println("\nWARNING: unable to wait for existing tasks to finish.");
            System.out.println("\nServer has terminated.\n\n\n");
        }));

        adminCLI();
        System.out.println("\nServer has terminated.\n\n\n");
    }

    private static void adminCLI(){

        Scanner sin = new Scanner(System.in);

        while( server.isRunning() ){
            System.out.print("(? for help)_>  ");
            String cmd = sin.nextLine().trim();

            if( cmd.isEmpty() ) continue;

            if( cmd.equals("?") ){
                System.out.println( help_menu() );
            }
//            else if( cmd.equalsIgnoreCase("show-workers") ){
//                for(Worker w : master.getWorkers())
//                    System.out.println( w.toString() );
//                System.out.println("\n");
//            }
//            else if( cmd.startsWith("add-worker") ){
//                Worker w = parseWorkerFromString(
//                        cmd.replaceFirst("add-worker", "").trim()
//                );
//                if( w == null ) {
//                    System.out.println("\nUnable to parse IP and PORT. Please check the syntax a try again.");
//                    continue;
//                }
//                boolean isNew = true;
//                for(Worker existing : master.getWorkers())
//                    if( existing.host.getAddress() == w.host.getAddress() ) {
//                        System.out.println("\nThat worker already exists in the workforce.");
//                        isNew = false; break;
//                    }
//                if( ! isNew )
//                    continue;
//                if( w.connect() ){
//                    ArrayList<Worker> tmp = new ArrayList<>();
//                    tmp.add(w);
//                    master.addWorkers( tmp );
//
//                    System.out.println("\nSave this worker to " + workersFile + " (y/n) ?  ");
//                    cmd = sin.nextLine().trim();
//                    if(cmd.equalsIgnoreCase("y"))
//                        report.write2file( workersFile,
//                                w.host.getAddress().getHostAddress()+":"+w.host.getPort(),
//                                true );
//                }
//                else
//                    System.out.println("Unable to connect to worker " + w.host.getAddress().getHostAddress()
//                            + " via port " + w.host.getPort());
//            }
//            else if( cmd.equalsIgnoreCase("show-last") ){
//                System.out.println("\n" + master.getLastUncheckedBaseMnemonic());
//            }
//            else if( cmd.equalsIgnoreCase("test-email") ){
//                System.out.println("\nTrying to send an email...\n");
//                report.testEmailSender();
//            }
//            else if( cmd.equalsIgnoreCase("halt") ){
//                System.out.println("\nWaiting (max of " + workerTerminationTimeout +
//                        " seconds) for the master thread to end.");
//                master.setRunning(false);
//                try {
//                    master.join(workerTerminationTimeout * 1000);
//                } catch (InterruptedException ignored) {}
//                System.exit(0);
//            }
//            else if( cmd.equalsIgnoreCase("force-halt") ){
//                master.setRunning(false);
//                System.exit(0);
//            }
            else
                System.out.println("\n(Unknown command!)\n");
        }
    }

    private static String help_menu(){
        return """

                \t reload-workers \t\t-\t re-read the workers file and try to connect to the new ones
                \t show-workers \t\t\t-\t display the list of workers being used
                \t add-worker IP[:PORT] \t-\t add the specified worker
                \t show-last \t\t\t\t-\t show the last unchecked mnemonic
                \t test-email \t\t\t-\t try sending a test email
                \t halt \t\t\t\t\t-\t exit orderly, waiting 30s max for workers to finish tasks and save last unchecked mnemonic
                \t force-halt \t\t\t-\t force master to end, not waiting for workers and save last unchecked mnemonic
                
                """;
    }

    private static void validateParams() {
        if ( Params.maxWorkQueueCapacity < 1 )
            throw new RuntimeException("maxWorkQueueCapacity parameter must be greater than zero");
        if ( Params.maxPendingTasks < 1 )
            throw new RuntimeException("maxPendingTasks parameter must be greater than zero");
        if ( Params.requestTimeoutMillis < 1 )
            throw new RuntimeException("requestTimeoutMillis parameter must be greater than zero");
    }

}
