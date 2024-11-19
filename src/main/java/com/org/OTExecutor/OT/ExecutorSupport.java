package com.org.OTExecutor.OT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.*;

public class ExecutorSupport {
    public enum TaskType {
        READ,
        WRITE,
    }
    //Test comment
    public interface TaskExecutor {
        /**
         * Submit new task to be queued and executed.
         *
         * @param task Task to be executed by the executor. Must not be null.
         * @return Future for the task asynchronous computation result.
         */
        <T> Future<T> submitTask(Task<T> task);
    }
    /**
     * Representation of computation to be performed by the {@link TaskExecutor}.
     *
     * @param taskUUID Unique task identifier.
     * @param taskGroup Task group.
     * @param taskType Task type.
     * @param taskAction Callable representing task computation and returning the result.
     * @param <T> Task computation result value type.
     */
    public record Task<T>(
            UUID taskUUID,
            TaskGroup taskGroup,
            TaskType taskType,
            Callable<T> taskAction
    ) {
        public Task {
            if (taskUUID == null || taskGroup == null || taskType == null || taskAction == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
        }
    }

    /**
     * Task group.
     *
     * @param groupUUID Unique group identifier.
     */
    public record TaskGroup(UUID groupUUID) {
        public TaskGroup {
            if (groupUUID == null) {
                throw new IllegalArgumentException("All parameters must not be null");
            }
        }
    }
    static class MyThreadPoolCustom implements TaskExecutor {
        Logger logger = LoggerFactory.getLogger(MyThreadPoolCustom.class);
        final ExecutorService executorService;
        public MyThreadPoolCustom(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public <T> Future<T> submitTask(Task<T> task) {
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();

            executorService.execute(() -> {
                try {
                    TaskGroup taskGroup = task.taskGroup();
                    taskGroup.groupUUID().getLeastSignificantBits();
                    System.out.println("UUID --> " + taskGroup.groupUUID().getLeastSignificantBits());
                    T callable = task.taskAction.call();
                    completableFuture.complete(callable);
                } catch (InterruptedException | ExecutionException e) {
                    completableFuture.completeExceptionally(e);
                } catch (Exception ex) {
                    logger.error("Exception occurred during task submission");
                }
            });
            return (Future<T>) completableFuture;
        }
    }
    /*
    *
    * */
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ExecutorSupport.class);
        ExecutorService executorService=Executors.newFixedThreadPool(4);
        MyThreadPoolCustom executor = new MyThreadPoolCustom(executorService); // Example with maximum concurrency of 4
        UUID uuid = UUID.randomUUID();
        TaskGroup groupOne = new TaskGroup(UUID.randomUUID());
        TaskGroup groupTwo = new TaskGroup(UUID.randomUUID());

        // Submit tasks
        Future<?> future1 = executor.submitTask(new ExecutorSupport.Task<>(uuid,groupOne,
                TaskType.READ,new ValueReturningTaskSecond(5,2,4000)));

        Future<?> future2 = executor.submitTask(new ExecutorSupport.Task<>(uuid,groupTwo,
                TaskType.WRITE,new ValueReturningTaskSecond(15,3,1000)));
        Future<?> future3 = executor.submitTask(new ExecutorSupport.Task<>(uuid,groupTwo,
                TaskType.WRITE,new ValueReturningTaskSecond(8,6,2000)));

        try {
            System.out.println(future1.get());
            System.out.println(future2.get());
            System.out.println(future3.get());
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Exception occurred while getting future object");
        }
        executorService.shutdown();
    }
}
