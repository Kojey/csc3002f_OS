import java.util.Scanner;

import simulator.Config;
import simulator.FCFSKernel;
import simulator.Kernel;
import simulator.RRKernel;
import simulator.SystemTimer;
import simulator.TRACE;


/**
 * Driver simulator for FCFS
 * @author othniel
 * @version April 2016
 */
public class SimulateRR {

	public static void main(String[] args) {
	
	String configFileName;
	int syscallCost,dispatchCost,level;
	long sliceTime;
	
	// Get configuration
	Scanner scanner = new Scanner(System.in);
	System.out.println("*** FCFS Simulator ***");
	System.out.print("Enter configuration file name: ");
	configFileName = scanner.nextLine();
	System.out.print("Enter slice time: ");
	sliceTime = scanner.nextInt();
	System.out.print("Enter cost of system call: ");
	syscallCost = scanner.nextInt();
	System.out.print("Enter cost of context switch: ");
	dispatchCost = scanner.nextInt();
	System.out.print("Enter trace level: ");
	level = scanner.nextInt();

	TRACE.SET_TRACE_LEVEL(level);
	final RRKernel kernel = new RRKernel();
	Config.init(kernel, dispatchCost, syscallCost);
	Config.buildConfiguration(configFileName);
	kernel.setSliceTime(sliceTime);
	Config.run();
	SystemTimer timer = Config.getSystemTimer();
	
	System.out.println(timer);
	System.out.println("Context switches: "+Config.getCPU().getContextSwitches());
	System.out.printf("CPU utilization: %.2f\n",((double)timer.getUserTime())/timer.getSystemTime()*100);
	
	}

}
