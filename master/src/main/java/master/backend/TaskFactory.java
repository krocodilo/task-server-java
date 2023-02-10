package master.backend;

import master.Params;
import me.myself.grpc.Task;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TaskFactory implements Runnable{

    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);
    private final AtomicLong taskId = new AtomicLong(1);
        // Should start at 1, because the default value in gRPC messages (when not set) is zero
        // Once it reaches maximum long value, it restarts with the minimum long value
    private final LinkedBlockingQueue<Task> workQueue;

    public TaskFactory() {
        this.workQueue = new LinkedBlockingQueue<>( Params.maxWorkQueueCapacity );
    }

    @Override
    public void run() {
        while( ! shutdown.get() ){
            try{
                long id = taskId.incrementAndGet();
                if( id == 0 )
                    id = taskId.incrementAndGet();  // cannot be zero

                workQueue.put(              // If queue is full, it will wait until space is available
                        Task.newBuilder()
                                .setId( id )
                                .setTask( String.valueOf( id ) )
                                .build()
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        isFinished.set(true);
    }

    /**
     * Insert tasks to work queue. Added elements will be removed from the list provided
     * @param tasks List to add to work queue
     * @return True, if all elements added. False, if some failed to add
     */
    public boolean insertTasks(List<Task> tasks) {
        try {
            for(Task t : tasks) {
                long id = taskId.incrementAndGet();
                if( id == 0 )
                    id = taskId.incrementAndGet();  // cannot be zero
                workQueue.put(
                        Task.newBuilder( t ).setId( id ).build()
                );
                tasks.remove(t);
            }
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public void shutdown() {
        shutdown.set( true );
        isFinished.set( true );
    }

    public LinkedBlockingQueue<Task> getWorkQueue() {
        return workQueue;
    }

    public boolean isFinished() {
        return isFinished.get();
    }
}
