package com.bada.thread.atomic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 
 * In this example we have implemented stack with standard and lockfree way and
 * doing mane pushing and popping operations,means using atomic refs. lockfree
 * make the output 200%
 * 
 * IN standard approach we have used synchronous block while in the lock free we
 * are using atomic
 * 
 * Atomic References, Compare And Set, Lock-Free High Performance Data Structure
 * https://www.udemy.com/java-multithreading-concurrency-performance-optimization
 */
public class AtomicReferenceExample {
	public static void main(String[] args) throws InterruptedException {
		StandardStack<Integer> stack = new StandardStack<>();
//		LockFreeStack<Integer> stack = new LockFreeStack<>();
		Random random = new Random();

		for (int i = 0; i < 100000; i++) {
			stack.push(random.nextInt());
		}

		List<Thread> threads = new ArrayList<>();

		int pushingThreads = 2;
		int poppingThreads = 2;

		for (int i = 0; i < pushingThreads; i++) {
			Thread thread = new Thread(() -> {
				while (true) {
					stack.push(random.nextInt());
				}
			});

			thread.setDaemon(true);
			threads.add(thread);
		}

		for (int i = 0; i < poppingThreads; i++) {
			Thread thread = new Thread(() -> {
				while (true) {
					stack.pop();
				}
			});

			thread.setDaemon(true);
			threads.add(thread);
		}

		for (Thread thread : threads) {
			thread.start();
		}

		Thread.sleep(10000);

		System.out.println(String.format("%,d operations were performed in 10 seconds ", stack.getCounter()));
	}

	public static class LockFreeStack<T> {
		private AtomicReference<StackNode<T>> head = new AtomicReference<>();
		private AtomicInteger counter = new AtomicInteger(0);

		public void push(T value) {
			StackNode<T> newHeadNode = new StackNode<>(value);

			// here we are making sure that there is no race conditions while pushing the
			// new data and update head; if any change in the head then wait and perform the
			// same; compareAndSet method will make sure the values are same as expected
			while (true) {
				StackNode<T> currentHeadNode = head.get();
				newHeadNode.next = currentHeadNode;
				if (head.compareAndSet(currentHeadNode, newHeadNode)) {
					break;
				} else {
					LockSupport.parkNanos(1);
				}
			}
			counter.incrementAndGet();
		}

		public T pop() {
			StackNode<T> currentHeadNode = head.get();
			StackNode<T> newHeadNode;

			// here we are making sure that there is no race conditions while pushing the
			// new data and update head; if any change in the head then wait and perform the
			// same
			while (currentHeadNode != null) {
				newHeadNode = currentHeadNode.next;
				if (head.compareAndSet(currentHeadNode, newHeadNode)) {
					break;
				} else {
					LockSupport.parkNanos(1);
					currentHeadNode = head.get();
				}
			}
			counter.incrementAndGet();
			return currentHeadNode != null ? currentHeadNode.value : null;
		}

		public int getCounter() {
			return counter.get();
		}
	}

	public static class StandardStack<T> {
		private StackNode<T> head;
		private int counter = 0;

		public synchronized void push(T value) {
			StackNode<T> newHead = new StackNode<>(value);
			newHead.next = head;
			head = newHead;
			counter++;
		}

		public synchronized T pop() {
			if (head == null) {
				counter++;
				return null;
			}

			T value = head.value;
			head = head.next;
			counter++;
			return value;
		}

		public int getCounter() {
			return counter;
		}
	}

	private static class StackNode<T> {
		public T value;
		public StackNode<T> next;

		public StackNode(T value) {
			this.value = value;
			this.next = next;
		}
	}
}
