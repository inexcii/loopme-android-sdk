package com.loopme;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorHelper {
	
	private static final ExecutorService sExecutor = Executors.newCachedThreadPool();
	
	private ExecutorHelper() {}

	public static ExecutorService getExecutor() {
		return sExecutor;
	}
	
	public static void shutdown() {
		sExecutor.shutdown();
	}
}
