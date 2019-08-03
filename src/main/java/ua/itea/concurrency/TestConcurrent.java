package ua.itea.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

public class TestConcurrent {
	static int commonTimeout = 40;
	static long start;

	public static void main(String[] args) {
//		System.out.println(Item.of(501));
//		System.out.println(Item.of(501));
		
//		try (Task<Item> task = Task.of(() -> {
//			System.out.println("started");
//			try {
//				Thread.sleep(110);
//				System.out.println("finished");
//			} catch (InterruptedException e) {
//				System.out.println("interrapted");
//			}
//		}, new Item(0, 0)).setInterrapted(true)) 
//		{
//			System.out.println(task.get(100, TimeUnit.MILLISECONDS));
//		} catch (Exception e) {
//			System.out.println("timeouted");
//		}
		
		
		start = System.currentTimeMillis();
		int size = 10;
		IntStream.range(0, size).forEach(i -> {
			System.out.print((size - i) + " -> ");
			System.out.println(Item.of(5, getTimeout(size - i))); //(int) (Math.random() * 10)
		});
		System.out.println("SumSleepTime = " + Item.sumSleepTime);
		System.out.println("SumTimeOut = " + Item.sumTimeOut);
		System.out.println("ExecutionTime = " + ((System.currentTimeMillis() - start)/1000));
	}
	
	public static int getTimeout(int unhandledAmount) {
		int usedTime = (int) ((System.currentTimeMillis() - start) / 1000);
		return (commonTimeout - usedTime) / unhandledAmount;
	}
	
	public static class Item {
		static long sumTimeOut;
		static long sumSleepTime;
		static int count;
		int number;
		long sleepTime;
		long timeOut;
		long time;
		
		Item(long sleepTime, long timeOut) {
			number = ++count;
			this.sleepTime = sleepTime;
			this.timeOut = timeOut;
			time = System.currentTimeMillis();
			try {
				Thread.sleep(sleepTime * 1000);
				sumTimeOut = sumTimeOut + timeOut;
				sumSleepTime = sumSleepTime + sleepTime;
			} catch (InterruptedException e) {
				System.out.println("Interrapted sleep for Item_" + number);
				//e.printStackTrace();
			} finally {
				time = (System.currentTimeMillis() - time)/1000;
				//System.out.println(count + " -> " + time);
			}
		}

		static Item of(int sleep, int timeOut) {
			Item test = Task.valueOf(() -> new Item(sleep, timeOut), timeOut, TimeUnit.SECONDS, true);
			return test == null ? new Item(0, timeOut) : test;
//			try (Task<Item> task = Task.of(() -> new Item(sleep, timeOut))) {
//				return task.get(timeOut, TimeUnit.SECONDS);
//			} catch (Exception e) {
//				Item test = new Item(0, timeOut);
//				System.out.println("Timeout for Item_" + (test.number - 1));
//				return test;
//			}
		}

		@Override
		public String toString() {
			return "Item [number=" + number + ", sleepTime=" + sleepTime + ", timeOut=" + timeOut + ", time=" + time
					+ "]";
		}
	}
	
	static class Task<V> implements Future<V>, AutoCloseable {
		private Future<V> future;
		private boolean isInterrapted;
		
		private Task(Future<V> future) {
			this.future = future;
		}
		
		public static <V> Task<V> of(Callable<V> task) {
			return new Task<V>(Exe.service().submit(task));
		}
		
		@SafeVarargs
		public static <V> Task<V> of(Runnable task, V...res) {
			return new Task<V>(Exe.service().submit(task, res != null && res.length > 0 ? res[0] : null));
		}
		
		public boolean isInterrapted() {
			return isInterrapted;
		}

		public Task<V> setInterrapted(boolean isInterrapted) {
			this.isInterrapted = isInterrapted;
			return this;
		}
		
		public static <V> V valueOf(Callable<V> task, boolean...isInterrapted) {
			try (Task<V> t = of(task).setInterrapted(isInterrapted != null && isInterrapted.length > 0 && isInterrapted[0])) {
				return t.get();
			} catch (Exception e) {
				return null;			
			}
		}
		
		public static <V> V valueOf(Callable<V> task, long timeout, TimeUnit unit, boolean...isInterrapted) {
			try (Task<V> t = of(task).setInterrapted(isInterrapted != null && isInterrapted.length > 0 && isInterrapted[0])) {
				return t.get(timeout, unit);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void close() throws Exception {
			if (future != null && !isDone()) cancel(isInterrapted);
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return future == null || future.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return future == null || future.isCancelled();
		}

		@Override
		public boolean isDone() {
			return future == null || future.isDone();
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return future == null ? null : future.get();
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return future == null ? null : future.get(timeout, unit);
		}
		
		public static class Exe {
			private static ExecutorService service;
			
			public static ExecutorService service() {
				if (service == null || service.isShutdown()) service = Executors.newCachedThreadPool();
				return service;
			}
			
		}
		
	}

}

