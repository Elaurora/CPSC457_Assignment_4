package testDriver;

import java.util.Scanner;

import memoryPackage.MainMemory;
import memoryPackage.MemoryAgent;
import memoryPackage.MutableInteger;
import memoryPackage.Processor;
import memoryPackage.WriteBuffer;

/**
 * Sets up and runs the simulation
 * @author Patrick
 *
 */
public class Tester {
	public static void main(String[] args) {
		
		//process arguments
		if(args.length < 1) {
			System.err.println("No store algorithm provided, correct invokation: java Tester [pso/tso]");
			return;
		}
		
		boolean TSO = args[0].equals("tso");
		
		
		int n = 10;
		if(args.length > 1) {
			n = Integer.parseInt(args[1]);
		}
		
		
		//create a main memory
		MainMemory mainMemory = new MainMemory();
		
		//create mutable integers
		MutableInteger a = new MutableInteger();
		MutableInteger b = new MutableInteger();
		
		//create n processors
		Processor[] processors = new Processor[n];
		
		//create n memoryAgents
		MemoryAgent[] memoryAgents = new MemoryAgent[n];
		
		for(int i = 0; i < n; i++) {
			WriteBuffer wb = new WriteBuffer(TSO, mainMemory);
			processors[i] = new Processor(i, n, wb, mainMemory,a, b);
			memoryAgents[i] = new MemoryAgent(wb, mainMemory);
		}
		
		System.out.println("Simulation Running, type \"quit\" to exit");
		
		for(int i = 0; i < n; i++) {
			processors[i].start();
			memoryAgents[i].start();
		}
		
		//poll user for quit command
		Scanner scan = new Scanner(System.in);
		
		while(true) {
			if(scan.next().equals("quit")) {
				break; 
			}
		}
		
		System.out.println("Simulation Ending, please wait...");
		
		scan.close();
	
		//tell the threads to quit
		for(int i = 0; i < processors.length; i++) {
			processors[i].done();
		}
		
		//wait for threads to finish
		try {
			for(int i = 0; i < processors.length; i++) {
				processors[i].join();
			}
		} catch (InterruptedException e) {
			//on interrupt just quit
		}
		
		
		//tell the threads to quit
		for(int i = 0; i < processors.length; i++) {
			memoryAgents[i].done();
		}
		
		//wait for threads to finish
		try {
			for(int i = 0; i < processors.length; i++) {
				memoryAgents[i].join();
			}
		} catch (InterruptedException e) {
			//on interrupt just quit
		}
		
		//print results
		System.out.println("Successful Critical Section Access:" + Processor.successTotal);
		System.out.println("Failed Critical Section Access:" + Processor.errorTotal);
		
	}
}
