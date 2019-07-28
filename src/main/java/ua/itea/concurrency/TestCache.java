package ua.itea.concurrency;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class TestCache {
	private static final TestCache singleton = new TestCache();
	private static final List<Map<String, String>> CACHE = new ArrayList<>();
	private static volatile LocalDateTime time;
	
	public synchronized static Cache getCache(boolean isInnerClass) {
		if (time == null || CACHE.isEmpty() || Duration.between(time, LocalDateTime.now()).toMillis() > 100) {
			if (!CACHE.isEmpty()) CACHE.clear();
			IntStream.range(1, 12).forEach(i -> {
				Map<String, String> map = new HashMap<>();
				IntStream.range(1, 12).forEach(j ->  map.put("key_" + j, "value_" + (i * j)));
				CACHE.add(map);
			});
			time = LocalDateTime.now();
		}
		return isInnerClass ? new Cache() : null;
	}
	
	// Tested approach - 'Singleton'
	public synchronized String get(int i) {
		return CACHE.get(i).keySet().stream().reduce("--Select--", (a, b) -> a + "|" + b);
	}
	
	// Tested approach - 'InnerClass'
	public static class Cache {
		private List<Map<String, String>> cache;
		
		private Cache() {
			cache = new ArrayList<>(CACHE);
		}
		
		public String get(int i) {
			return cache.get(i).keySet().stream().reduce("--Select--", (a, b) -> a + "|" + b);
		}
	}

	// Testing code:
	public static void main(String[] args) {
		int rand = (int) (Math.random() * 10); 
		doTest(rand % 2 == 0);
		doTest(rand % 2 == 1);
	}
	
	public static boolean doTest(boolean isInnerClass) {
		boolean[] commonSuccess = {true};
		List<Long> time = new ArrayList<>();
		int iterations = 100;
		ExecutorService pool = Executors.newFixedThreadPool(10);
		IntStream.range(0, 11).forEach(i -> {
			long start = System.currentTimeMillis();
			boolean[] isSuccess = {true};
			CountDownLatch latch = new CountDownLatch(iterations);
			IntStream.range(0, iterations).forEach(j -> pool.execute(() -> {
				Cache innerClass = getCache(isInnerClass);
				try {
					Thread.sleep(105);
					if (isInnerClass) {
						innerClass.get((int) (Math.random() * 10));
					} else {
						singleton.get((int) (Math.random() * 10));
					}
				} catch (Exception e) {
					commonSuccess[0] = false;
					isSuccess[0] = false;
					//e.printStackTrace();
				} finally {
					latch.countDown();
				}
			}));
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i > 0) {
				String mark = (isInnerClass ? "InnerClassTest_" : "SingletonTest_") + i + " -> ";
				time.add(System.currentTimeMillis() - start);
				(isSuccess[0] ? System.out : System.err).println(mark + (isSuccess[0] ? "Success" : "Failed") + ";  Time: " + time.get(time.size() - 1));
			}
		});
		pool.shutdown();
		double avgTime = time.stream().mapToLong(Long::valueOf).summaryStatistics().getAverage();
		(commonSuccess[0] ? System.out : System.err).println((isInnerClass ? "INNER_CLASS:  " : "SINGLETON:  ") + (commonSuccess[0] ? "SUCCESS" : "FAILED") + ";  AvgTime = " + avgTime);
		return commonSuccess[0];
	}

}
