/*
 * Instructions
 * Use javac then java to compile and run Museum.java like a regular java file
 * 
 * Run the program a few different times to see how different threads interleave
 * 
 * Assumptions
 * The Museum starts out empty and closed. If the Museum is closed and empty the 
 * Museum is automatically signaled to open. 2 interleaving threads randomly generate
 * enter or exit actions (if logical conditions for entering or exiting are satisfied)
 * There can be a maximum of 5 people in the museum at a time. The signal to close the
 * Museum is generated randomly. The system runs for a finite time and terminates only 
 * when the Museum is empty.
 */

import java.util.concurrent.Semaphore;

public class Museum extends Thread{
	public static final int N = 5;
	
	public static class Counter{
		private int value = 0;
		private int Iter = 0;
		private boolean open = false;
		
		public int getValue() {
			return value;
		}

		public void enter() {
			int reg = this.value;
			reg += 1;
			this.value = reg;
		}
		
		public void exit() {
			int reg = this.value;
			reg -= 1;
			this.value = reg;
		}
		
		public void closeSignal() {
			this.open = false;
		}
		
		public void openSignal() {
			this.open = true;
		}
	}
	
	static Semaphore mutex = new Semaphore(1);
	Counter counter = null;
	
	public Museum(Counter sharedCounter) {
		super();
		this.counter = sharedCounter;
		System.out.println("Thread Created");
	}
	
	@Override
	public void run() {
		boolean done = false;
		while(!done) {
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//the museum is closed
			if (!this.counter.open) {
				//if the director has signalled that the museum is closed and the person is still inside then the person will exit
				if (this.counter.getValue() > 0) {
					this.counter.exit();
					System.out.println(this.getName() + " exit");
				}
				
			//the museum is open
			} else {
				int randomNum = (Math.random() <= 0.5) ? 1 : 2;
				
				if (randomNum == 1) {
					if (this.counter.getValue() < N) {
						this.counter.enter();
						System.out.println(this.getName() + " enter");
						this.counter.Iter +=1;
					}
				} else {
					if (this.counter.getValue() > 0) {
						this.counter.exit();
						System.out.println(this.getName() + " exit");
					}
				}
			}
			
			
			//if the museum is closed and has been emptied automatically signal for it to open
			if (this.counter.getValue() == 0 && !this.counter.open) {
				System.out.println("Museum is open");
				this.counter.openSignal();
			}
			if (((int)(Math.random() * ((5 - 1) + 1)) + 1) == 1 && this.counter.open) {
				System.out.println("Museum is closed");
				this.counter.closeSignal();
			}
			if (this.counter.Iter > 5 && this.counter.getValue() == 0) {
				done = true;
			}
			mutex.release();
		}
	}

	public static void main(String[] args) {
		Counter counter = new Counter();
		Museum p1 = new Museum(counter);
		Museum p2 = new Museum(counter);

		p1.start();
		p2.start();
		try {
			p1.join();
			p2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
