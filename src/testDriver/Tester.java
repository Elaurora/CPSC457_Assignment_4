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
		
		//create n processors
		Processor[] processors = new Processor[n];
		
		//create n memoryAgents
		MemoryAgent[] memoryAgents = new MemoryAgent[n];
		
		for(int i = 0; i < n; i++) {
			WriteBuffer wb = new WriteBuffer(TSO);
			processors[i] = new Processor(i, n, wb, mainMemory);
			memoryAgents[i] = new MemoryAgent(wb, mainMemory);
		}
		
		for(int i = 0; i < n; i++) {
			processors[i].start();
			memoryAgents[i].start();
		}
		
		//poll user for quit command
		Scanner scan = new Scanner(System.in);
		System.out.println("Simulation Running, type \"quit\" to exit");
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
			memoryAgents[i].done();
		}
		
		//wait for threads to finish
		try {
			for(int i = 0; i < processors.length; i++) {
				processors[i].join();
				memoryAgents[i].join();
			}
		} catch (InterruptedException e) {
			//on interrupt just quit
		}
		
	}
}
