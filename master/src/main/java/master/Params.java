package master;

public interface Params {

    /**
     * Defining parameters in an interface file is just a quick way of configuring this software. But ideally,
     * configuration should be read from a file that the admin can quickly edit, such as a config.ini type of file
     */

    int maxWorkQueueCapacity = 1000;    // Maximum tasks in memory, ready to be distributed

    int maxPendingTasks = 500;      // Maximum number of tasks pending. Once this limit is reached,
                // the oldest pending task will be redistributed to another worker

    int requestTimeoutMillis = 1000;    // Milliseconds

    String savefileFilename = "savefile.json";
}
