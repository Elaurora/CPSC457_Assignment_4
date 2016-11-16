package testDriver;

import java.util.Scanner;

import memoryPackage.MainMemory;
import memoryPackage.MemoryAgent;
import memoryPackage.Processor;
import memoryPackage.WriteBuffer;

/**
 * Sets up and runs the simulation
 * @author Patrick
 *
 */
public class Tester {
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.err.println("No store algorithm provided, correct invokation: java Tester [pso/tsp]");
			return;
		}
		//create a main memory
		MainMemory mainMemory = new MainMemory();
		
		boolean TSO = args[0].equals("tso");
		//create 2 writeBuffers
		WriteBuffer wb1 = new WriteBuffer(TSO);
		WriteBuffer wb2 = new WriteBuffer(TSO);
		
		//create 2 processors
		Processor p1 = new Processor(wb1, mainMemory);
		Processor p2 = new Processor(wb2, mainMemory);
		
		p1.start();
		p2.start();
	
		
		//create 2 memory agents
		MemoryAgent ma1 = new MemoryAgent(wb1, mainMemory);
		MemoryAgent ma2 = new MemoryAgent(wb2, mainMemory);
		
		ma1.start();
		ma2.start();
		
		//poll user for quit command
		Scanner scan = new Scanner(System.in);
		System.out.println("Simulation Running, type \"quit\" to exit");
		while(true) {
			String next = scan.next();
			if(next.equals("quit")) {
				break; 
			}
		}
		
		scan.close();
		
		//tell the threads to quit
		p1.done();
		p2.done();
		ma1.done();
		ma2.done();
		
		//wait for threads to finish
		try {
			p1.join();
			p2.join();
			ma1.join();
			ma2.join();
		} catch (InterruptedException e) {
			//on interrupt just quit
		}
		
	}
}
