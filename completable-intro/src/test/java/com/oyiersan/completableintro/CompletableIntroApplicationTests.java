package com.oyiersan.completableintro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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



    // 异步计算 链式调用方式
    @Test
    public void testCompletableFutureThenApplyAccept() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .thenRun(this::notifyByEmail).join();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

    }

    private void notifyByEmail() {
        System.out.println(Thread.currentThread() + " notifyByEmail...");
        // business code
//        System.out.println("send notify by email ..." + d);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Double notifyBalance(Double d) {
        System.out.println(Thread.currentThread() + " notifyBalance...");
        // business code
//        System.out.println(String.format("your balance is $%s", d));

//        int a = 1/0;
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 1D + d;
    }

    private Double calculateBalance(Double d) {
        System.out.println(Thread.currentThread() + " calculateBalance...");
        try {
            int a = 1/0;
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // business code
        return 1D + d;
    }

    private Double findAccountNumber() {
        System.out.println(Thread.currentThread() + " findAccountNumber...");

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // business code
        return 1D;
    }

    // 异步计算 任务线程单独执行
    @Test
    public void testCompletableFutureApplyAsync() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(5);
                CompletableFuture
                        .supplyAsync(this::findAccountNumber, newFixedThreadPool)
                        .thenApplyAsync(this::calculateBalance,
                                newFixedThreadPool)
                        .thenApplyAsync(this::notifyBalance, newFixedThreadPool)
                        .thenRunAsync(this::notifyByEmail, newFixedThreadPool).join();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }


    // 执行结果处理 thenCompose
    @Test
    public void testCompletableFutureThenCompose() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Double balance = this.doFindAccountNumber()
                .thenCompose(this::doCalculateBalance)
                .thenCompose(this::doSendNotifyBalance).join();
        assertEquals(1D, balance);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    private CompletableFuture<Double> doSendNotifyBalance(Double aDouble) {
        sleepSeconds(5);
        // business code
        System.out.println(Thread.currentThread() + " doSendNotifyBalance...");

        return CompletableFuture.completedFuture(1D);
    }

    private CompletableFuture<Double> doCalculateBalance(Double d) {
        sleepSeconds(5);
        // business code
        System.out.println(Thread.currentThread() + " doCalculateBalance...");

        return CompletableFuture.completedFuture(1D);
    }

    private CompletableFuture<Double> doFindAccountNumber() {
        sleepSeconds(5);
        // business code
        System.out.println(Thread.currentThread() + " doFindAccountNumber...");

        return CompletableFuture.completedFuture(1D);
    }

    private void sleepSeconds(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // 执行结果处理 thenCombine 合并多个独立任务的处理结果
    @Test
    public void testCompletableFutureThenCombine() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompletableFuture<String> thenCombine = this.findName().thenCombine(this.findAddress(), (name, address) -> name + address);
        String personInfo = thenCombine.join();
        assertEquals("thenCombine Shanghai, China", personInfo);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    private CompletableFuture<String> findAddress() {
        return CompletableFuture.supplyAsync(() -> {
            sleepSeconds(5);
            // business code
            return "Shanghai, China";
        });
    }

    private CompletableFuture<String> findName() {
        return CompletableFuture.supplyAsync(() -> {
            sleepSeconds(5);
            // business code
            return "thenCombine ";
        });
    }


    // 等待多个任务执行完成 Allof
    @Test
    public void testCompletableFutureAllof() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<CompletableFuture<String>> list = new ArrayList<>();
        IntStream.range(0, 3).forEach(num -> list.add(findName(num)));

        CompletableFuture<Void> allFuture = CompletableFuture
                .allOf(list.toArray(new CompletableFuture[0]));

        CompletableFuture<List<String>> allFutureList = allFuture
                .thenApply(val -> {
                    List<String> result = list.stream().map(CompletableFuture::join).collect(Collectors.toList());
                    return result;
                });

        CompletableFuture<String> futureHavingAllValues = allFutureList
                .thenApply(fn -> String.join("", fn));

        String result = futureHavingAllValues.join();
        assertEquals("Allof0Allof1Allof2", result);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    private CompletableFuture<String> findName(int num) {
        return CompletableFuture.supplyAsync(() -> {
            sleepSeconds(2);
            // business code
            return "Allof" + num;
        });
    }

    @Test
    public void whenFutureCombinedWithAllOfCompletes_thenAllFuturesAreDone() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Beautiful";});
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "World");

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

        // ...

//        Void join = combinedFuture.join();
//        combinedFuture.get();

//        assertTrue(future1.isDone());
//        assertTrue(future2.isDone());
//        assertTrue(future3.isDone());

        String combined = Stream.of(future1, future2, future3)
                .map(CompletableFuture::join)
                .collect(Collectors.joining(" "));

        System.out.println(combined);
        assertEquals("Hello Beautiful World", combined);
    }


    //异常处理  exceptionally
    @Test
    public void testCompletableFutureExceptionally() {
        CompletableFuture<Double> thenApply = CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .exceptionally(ex -> {
                    System.out.println("Exception " + ex.getMessage());
                    return 0D;
                });
        Double join = thenApply.join();
        assertEquals(0D, join);
    }


    //异常处理  handle
    @Test
    public void testCompletableFutureHandle() {
        Double join = CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .handle((ok, ex) -> {
                    System.out.println("最终要运行的代码...");
                    if (ok != null) {
                        System.out.println("No Exception !!");
                    } else {
                        System.out.println("Exception " + ex.getMessage());
                        return -1D;
                    }
                    return ok;
                }).join();
        assertEquals(-1D, join);

    }

    //异常处理  whenComplete
    @Test
    public void testCompletableFutureWhenComplete() {
        CompletableFuture.supplyAsync(this::findAccountNumber)
                .thenApply(this::calculateBalance)
                .thenApply(this::notifyBalance)
                .whenComplete((result, ex) -> {
                    System.out.println("result = " + result + ", ex = " + ex);
                    System.out.println("最终要运行的代码...");
                }).join();
    }



}
