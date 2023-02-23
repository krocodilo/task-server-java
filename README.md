# Task Server

Work-In-Progress.

This is a Java implementation of the Master-Worker architecture, for distribution of tasks.
It uses gRPC for communication, which might be the fastest API protocol/framework currently available.

This project is split into three modules: grpc-common; master; worker.

## To-Do
- handle all possible communication errors
- Communications are currently unencrypted
- create admin console on server
- task creator must have method to return the current progress 

## Instructions
#### gRPC Configuration
- Configure `grpc-common/.../config.proto`
- Execute Maven Clean
- Execute Maven Install


## Some Notes
- LinkedBlockingQueue seems to be the best List/Queue type to be used, as it is ThreadSafe and has two different locks: one for .take(), when the queue is empty; and another for .put(), for when the list is full
- gRPC Channels and Stubs are Thread Safe
- Async Stubs on gRPC are used primarily for data streaming, but can also be configured to run tasks when server send .onCompleted(), providing you save locally the data received via .onNext()
- gRPC can also create a FutureStub, which allows for simple (non-streaming) Async requests. You have to specify a runable that will be called once a server response is receievd and the ThreadExecutor where it will run.










