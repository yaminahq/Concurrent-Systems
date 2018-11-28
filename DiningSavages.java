/*
 * Instructions
 * Run the program a few different times to see how different threads interleave
 * 
 * Assumptions
 * This solution models the Dining Savages system. In this solution we assume 3 savages,
 * 1 cook and a pot that holds a maximum of M=3 servings at a time. The model
 * starts off with a full pot. Any time a savage goes to get a serving but finds the pot is
 * empty, the pot is refilled to have M servings by the implicitly-represented cook. The system
 * runs until the pot has been emptied 2 times. The number of savages can
 * be modified by creating additional instances of the DiningSavages class and then
 * starting and joining them in the main method. The capacity of the pot can be changed
 * by modifying the M constant in the DiningSavages class. The number of refills and
 * subsequent emptying of the pot before the system terminates can be modified by 
 * changing the value of the PotEmptied  in the DiningSavages class.
 */
import java.util.concurrent.Semaphore;

public class DiningSavages extends Thread{
	public static final int M = 5;
	public static final int PotEmptied = 2;
	
	public static class Pot{
		private int value = M;
		private int numRefills = 0;
		
		public int getValue() {
			return value;
		}
		
		public int getNumRefills() {
			return numRefills;
		}

		public void fill_pot() {
			int reg = this.value;
			reg += M;
			this.value = reg;
			int reg2 = this.numRefills;
			reg2 += 1;
			this.numRefills = reg2;
		}
		
		public void get_serving() {
			int reg = this.value;
			reg -= 1;
			this.value = reg;
		}
	}
	
	static Semaphore mutex = new Semaphore(1);
	Pot pot = null;
	
	public DiningSavages(Pot sharedPot) {
		super();
		this.pot = sharedPot;
		System.out.println(this.getName() + ": created");
	}
	
	@Override
	public void run() {
		boolean done = false;
		while(this.pot.getNumRefills() < PotEmptied) {
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println(this.getName() + " interrupted");
			}
			if (this.pot.getValue() == 0 && this.pot.getNumRefills() < PotEmptied) {
				this.pot.fill_pot();
				System.out.println("Pot is refilled");
			} else if(this.pot.getValue() != 0 && this.pot.getNumRefills() < PotEmptied) {
				System.out.println(this.getName() + ": Savage got a serving");
				this.pot.get_serving();
			}
			mutex.release();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Here we model a representation of the Dining Savages system.");
		System.out.println("We assume 3 savages, 1 arbitrary cook and a pot with M=3");
		System.out.println("servings. The pot starts off full and we run the system until");
		System.out.println("the pot has been emptied twice.\n");
		Pot pot = new Pot();
		DiningSavages s1 = new DiningSavages(pot);
		DiningSavages s2 = new DiningSavages(pot);
		DiningSavages s3 = new DiningSavages(pot);

		s1.start();
		s2.start();
		s3.start();
		
		try {
			s1.join();
			s2.join();
			s3.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	


}