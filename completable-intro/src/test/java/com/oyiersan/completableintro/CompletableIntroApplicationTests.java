package com.oyiersan.completableintro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CompletableIntroApplicationTests {


    // 异步处理 runAsync
    @Test
    public void testCompletableFutureRunAsync() {
        AtomicInteger variable = new AtomicInteger(0);
        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> process(variable));
        runAsync.join();
        assertEquals(100, variable.get());
    }

    public void process(AtomicInteger variable) {
        System.out.println(Thread.currentThread() + " Process...");
        variable.set(100);
    }


    // 异步处理 supplyAsync
    @Test
    public void testCompletableFutureSupplyAsync() {
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(this::process);
        try {
            assertEquals("Hello supplyAsync", supplyAsync.get()); // Blocking
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String process() {
        System.out.println(Thread.currentThread() + " Process...");

        return "Hello supplyAsync";
    }

    // 异步处理 自定义线程
    @Test
    public void testCompletableFutureSupplyAsyncWithExecutor() {
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(2);
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(this::process, newFixedThreadPool);
        try {
            assertEquals("Hello supplyAsync", supplyAsync.get()); // Blocking
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    AtomicInteger variable = new AtomicInteger(0);

    // 异步处理 thenApply
    @Test
    public void testCompletableFutureThenApply() {
        Integer notificationId = CompletableFuture.supplyAsync(this::thenApplyProcess)
                .thenApply(this::thenApplyNotify)
                .join();
        assertEquals(new Integer(1), notificationId);
    }

    // 异步处理 thenAccept
    @Test
    public void testCompletableFutureThenAccept() {
        CompletableFuture.supplyAsync(this::processVariable)
                .thenAccept(this::thenAcceptNotify)
                .join();
        assertEquals(100, variable.get());
    }

    // 异步处理 thenRun
    @Test
    public void testCompletableFutureThenRun() {
        CompletableFuture.supplyAsync(this::processVariable)
                .thenRun(this::thenRunNotify)
                .join();
        assertEquals(100, variable.get());
    }

    private String processVariable() {
        System.out.println(Thread.currentThread() + " processVariable...");

        variable.set(100);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "success";
    }

    private void thenRunNotify() {
        System.out.println(Thread.currentThread() + "thenRun completed notify ....");
    }

    private Integer thenApplyNotify(Integer integer) {
        System.out.println(Thread.currentThread() + " thenApplyNotify...");

        return integer;
    }

    private void thenAcceptNotify(String s) {
        System.out.println(Thread.currentThread() + " thenAcceptNotify..." + s);
    }

    public Integer thenApplyProcess()  {
        System.out.println(Thread.currentThread() + " thenApplyProcess...");

        return 1;
    }



    // 异步计算
    @Test
    public void testCompletableFutureThenApplyAccept() {
        CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .thenAccept(this::notifyByEmail);
    }

    private void notifyByEmail(Double d) {
        System.out.println(Thread.currentThread() + " notifyByEmail...");
        // business code
        System.out.println("send notify by email ..." + d);
    }

    private Double notifyBalance(Double d) {
        System.out.println(Thread.currentThread() + " notifyBalance...");
        // business code
//        System.out.println(String.format("your balance is $%s", d));
        return 1D + d;
    }

    private Double calculateBalance(Double d) {
        System.out.println(Thread.currentThread() + " calculateBalance...");
        // business code
        return 1D + d;
    }

    private Double findAccountNumber() {
        System.out.println(Thread.currentThread() + " findAccountNumber...");

        // business code
        return 1D;
    }




}
