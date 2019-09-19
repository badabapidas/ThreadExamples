package com.bada.thread.termination;

/**
 * Thread Termination & Daemon Threads
 * https://www.udemy.com/java-multithreading-concurrency-performance-optimization
 */
public class Main1 {
	public static void main(String[] args) {
		Thread thread = new Thread(new BlockingTask());
		thread.start();
	}

	private static class BlockingTask implements Runnable {

		@Override
		public void run() {
			// do things
			try {
				System.out.println("Long running thread");
				Thread.sleep(500000);
			} catch (InterruptedException e) {
				System.out.println("Existing blocking thread");
			}
		}
	}
}
