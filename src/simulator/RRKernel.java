package simulator;

//
import java.io.FileNotFoundException;
import java.io.IOException;
//
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

import simulator.ProcessControlBlock.State;

/**
 * Concrete Kernel type
 * 
 * @author Othniel Konan
 * @version April 2016
 */
public class RRKernel implements Kernel {
    

    private Deque<ProcessControlBlock> readyQueue;
    private long sliceTime;
    
    public RRKernel() {
		// Set up the ready queue.
    	readyQueue = new ArrayDeque<ProcessControlBlock>();
    }
    
    private ProcessControlBlock dispatch() {
		
		// Perform context switch, swapping process
		// currently on CPU with one at front of ready queue.
		CPU cpu = Config.getCPU();
		try {
			ProcessControlBlock process = this.readyQueue.pop();
			ProcessControlBlock outProcess = cpu.contextSwitch(process);
			// Set timeOut
			TimeOutEvent event = new TimeOutEvent(Config.getSystemTimer().getSystemTime()+this.sliceTime, process.getPID(), 
			      new EventHandler<TimeOutEvent>() {
	                public void process(TimeOutEvent event) {
		                    TRACE.INTERRUPT(InterruptHandler.TIME_OUT, Config.getCPU().getCurrentProcess());
					        Config.getSimulationClock().logInterrupt();
					        interrupt(InterruptHandler.TIME_OUT,Config.getCPU().getCurrentProcess());
					        TRACE.INTERRUPT_END();
	                }                            
			});
			Config.getEventScheduler().schedule(event);			
			process.setState(State.RUNNING);
			return outProcess;
		} 
		// If ready queue is empty then CPU goes idle ( holds a null value).
		// Returns process removed from CPU.
		catch (NoSuchElementException e) {return cpu.contextSwitch(null);}
    }
            
    
                
    public int syscall(int number, Object... varargs) {
        int result = 0;
        switch (number) {
             case MAKE_DEVICE:
                {
                    IODevice device = new IODevice((Integer)varargs[0], (String)varargs[1]);
                    Config.addDevice(device);
                }
                break;
             case EXECVE: 
                {
                    ProcessControlBlock pcb = RRKernel.loadProgram((String)varargs[0]);
                    if (pcb!=null) {
                        // Loaded successfully.
						// Now add to end of ready queue.
                    	this.readyQueue.add(pcb);
						// If CPU idle then call dispatch.
                    	if(Config.getCPU().isIdle())this.dispatch();
                    }
                    else {
                        result = -1;
                    }
                }
                break;
             case IO_REQUEST: 
                {
					// IO request has come from process currently on the CPU.
					// Get PCB from CPU.
                	ProcessControlBlock process = Config.getCPU().getCurrentProcess();
					// Find IODevice with given ID: Config.getDevice((Integer)varargs[0]);
                	IODevice ioDevice = Config.getDevice((Integer)varargs[0]);
					// Make IO request on device providing burst time (varages[1]),
					// the PCB of the requesting process, and a reference to this kernel (so // that the IODevice can call interrupt() when the request is completed.
                	ioDevice.requestIO((Integer)varargs[1], process, this);
					// Set the PCB state of the requesting process to WAITING.
                	process.setState(State.WAITING);
					// Call dispatch().
                	this.dispatch();
                	
                }
                break;
             case TERMINATE_PROCESS:
                {
					// Process on the CPU has terminated.
					// Get PCB from CPU.
					// Set status to TERMINATED.
                	Config.getCPU().getCurrentProcess().setState(State.TERMINATED);
                    // Call dispatch().
                	this.dispatch();
                }
                break;
             default:
                result = -1;
        }
        return result;
    }
   
    
    public void interrupt(int interruptType, Object... varargs){
        switch (interruptType) {
            case TIME_OUT:
            	break;
                //throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): this kernel does not suppor timeouts.");
            case WAKE_UP:
				// IODevice has finished an IO request for a process.
				// Retrieve the PCB of the process (varargs[1]), set its state
				// to READY, put it on the end of the ready queue.
            	ProcessControlBlock process = (ProcessControlBlock)varargs[1];
            	process.setState(State.READY);
            	this.readyQueue.add(process);
				// If CPU is idle then dispatch().
            	if(Config.getCPU().isIdle())this.dispatch();
                break;
            default:
                throw new IllegalArgumentException("FCFSKernel:interrupt("+interruptType+"...): unknown type.");
        }
    }
    
    private static ProcessControlBlock loadProgram(String filename) {
        try {
            return ProcessControlBlockImpl.loadProgram(filename);
        }
        catch (FileNotFoundException fileExp) {
            return null;
        }
        catch (IOException ioExp) {
            return null;
        }
    }
    
    public void setSliceTime(long time){
    	this.sliceTime = time;
    }
}
