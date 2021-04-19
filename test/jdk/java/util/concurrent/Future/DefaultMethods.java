/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @summary Tests for default methods defined by j.u.c.Future
 * @run testng DefaultMethods
 */

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Test
public class DefaultMethods {

    /**
     * Test isCompletedNormally when the task has already completed.
     */
    public void testIsCompletedNormally1() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<String> future = submit(executor, () -> "foo");
            await(future);
            assertTrue(future.isCompletedNormally());
        }
    }

    /**
     * Test isCompletedNormally when the task has not completed.
     */
    public void testIsCompletedNormally2() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });
            try {
                assertFalse(future.isCompletedNormally());
            } finally {
                future.cancel(true); // interrupt sleep
            }
        }
    }

    /**
     * Test isCompletedNormally when the task has completed with an exception.
     */
    public void testIsCompletedNormally3() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> { throw new RuntimeException(); });
            await(future);
            assertFalse(future.isCompletedNormally());
        }
    }

    /**
     * Test isCompletedNormally when the task is cancelled.
     */
    public void testIsCompletedNormally4() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });
            future.cancel(true);
            assertFalse(future.isCompletedNormally());
        }
    }

    /**
     * Test isCompletedNormally with the interrupt status and the task has
     * already completed.
     */
    public void testIsCompletedNormally5() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<String> future = submit(executor, () -> "foo");
            await(future);

            Thread.currentThread().interrupt();
            try {
                assertTrue(future.isCompletedNormally());
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test isCompletedNormally with the interrupt status set and when
     * the task has not completed.
     */
    public void testIsCompletedNormally6() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });

            Thread.currentThread().interrupt();
            try {
                assertFalse(future.isCompletedNormally());
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
                future.cancel(true);  // interrupt sleep
            }
        }
    }

    /**
     * Test isCompletedNormally with the interrupt status and the task has
     * already completed with an exception.
     */
    public void testIsCompletedNormally7() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> { throw new RuntimeException(); });
            await(future);

            Thread.currentThread().interrupt();
            try {
                assertFalse(future.isCompletedNormally());
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test isCompletedNormally with the interrupt status and the task is
     * cancelled.
     */
    public void testIsCompletedNormally8() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });
            future.cancel(true);

            Thread.currentThread().interrupt();
            try {
                assertFalse(future.isCompletedNormally());
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test join when the task has already completed.
     */
    public void testJoin1() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<String> future = submit(executor, () -> "foo");
            await(future);
            assertEquals(future.join(), "foo");
        }
    }

    /**
     * Test join when the task has already completed with an exception.
     */
    public void testJoin2() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> { throw new RuntimeException(); });
            await(future);
            expectThrows(CompletionException.class, future::join);
        }
    }

    /**
     * Test join when the task is cancelled.
     */
    public void testJoin3() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });
            future.cancel(true);
            expectThrows(CancellationException.class, future::join);
        }
    }

    /**
     * Test join waiting for a task to complete, task completes normally.
     */
    public void testJoin4() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<String> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(1));
                return "foo";
            });
            assertEquals(future.join(), "foo");
        }
    }

    /**
     * Test join waiting for a task to complete, task completes with exception.
     */
    public void testJoin5() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(1));
                throw new RuntimeException(); 
            });
            expectThrows(CompletionException.class, future::join);
        }
    }

    /**
     * Test join waiting for a task to complete, task is cancelled while waiting.
     */
    public void testJoin6() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });
            scheduleCancel(future, Duration.ofSeconds(1));
            expectThrows(CancellationException.class, future::join);
        }
    }

    /**
     * Test join waiting for a task to complete with the interrupt status set,
     * task completes normally.
     */
    public void testJoin7() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(1));
                return "foo";
            });

            Thread.currentThread().interrupt();
            try {
                assertEquals(future.join(), "foo");
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test join waiting for a task to complete with the interrupt status set,
     * task completes with an exception.
     */
    public void testJoin8() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(1));
                throw new RuntimeException();
            });

            Thread.currentThread().interrupt();
            try {
                expectThrows(CompletionException.class, future::join);
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test join waiting for a task to complete with the interrupt status set,
     * task is cancelled while waiting.
     */
    public void testJoin9() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(60));
                return null;
            });

            scheduleCancel(future, Duration.ofSeconds(1));
            Thread.currentThread().interrupt();
            try {
                expectThrows(CancellationException.class, future::join);
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Test join waiting for a task to complete. The thread is interrupted while
     * waiting., the task completes normally.
     */
    public void testJoin10() {
        try (var executor = Executors.newCachedThreadPool()) {
            Future<?> future = submit(executor, () -> {
                Thread.sleep(Duration.ofSeconds(5));
                return "foo";
            });

            // schedule thread to be interrupted
            scheduleInterrupt(Thread.currentThread(), Duration.ofMillis(500));
            try {
                assertEquals(future.join(), "foo");
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        }
    }

    /**
     * Wraps a Future with another Future object that delegates. The wrapper
     * does not override the default methods to allow them to be tested.
     */
    private static <V> Future<V> wrap(Future<V> future) {
        return new Future<V>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }
            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }
            @Override
            public boolean isDone() {
                return future.isDone();
            }
            @Override
            public V get() throws InterruptedException, ExecutionException {
                return future.get();
            }
            @Override
            public V get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return future.get(timeout, unit);
            }
        };
    }

    /**
     * Submits the task to the executor and wraps the Future so that its
     * default methods can be tested.
     */
    private static <V> Future<V> submit(ExecutorService executor, Callable<V> task) {
        return wrap(executor.submit(task));
    }

    /**
     * Waits for the future to be done.
     */
    private static void await(Future<?> future) {
        boolean interrupted = false;
        while (!future.isDone()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Schedules a future to be cancelled after the given delay.
     */
    private static void scheduleCancel(Future<?> future, Duration delay) {
        long millis = delay.toMillis();
        SES.schedule(() -> future.cancel(true), millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a thread to be interrupted after the given delay.
     */
    private static void scheduleInterrupt(Thread thread, Duration delay) {
        long millis = delay.toMillis();
        SES.schedule(thread::interrupt, millis, TimeUnit.MILLISECONDS);
    }

    private static final ScheduledExecutorService SES;
    static {
        ThreadFactory factory = (task) -> {
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            return thread;
        };
        SES = Executors.newSingleThreadScheduledExecutor(factory);
    }
}
