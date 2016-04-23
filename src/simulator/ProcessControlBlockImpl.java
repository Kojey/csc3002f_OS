package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//
import java.util.ArrayList;

/**
 * Description of Process Control Block used by kernel and simulator.
 * 
 * @author Stephan Jamieson
 * @version April 2016
 */

public class ProcessControlBlockImpl implements ProcessControlBlock {
	
	private int PID, priority, PC;
	private String programName;
	private State state;
	private ArrayList<Instruction> instruction = new ArrayList<Instruction>();
	
	public static ProcessControlBlock loadProgram(String filename) throws IOException, FileNotFoundException{
		try (BufferedReader buffer = new BufferedReader(new FileReader(filename))){
			String line;
			String [] lines;
			ProcessControlBlockImpl PCB = new ProcessControlBlockImpl();
			while((line=buffer.readLine())!=null){
				lines = line.split(" ");
				switch(lines.length){
				case 2: // CPU Instruction
					if(!lines[0].equals("CPU"))return null;
					PCB.instruction.add(new CPUInstruction(Integer.parseInt(lines[1])));
					break;
				case 3: // IO Instruction
					if(!lines[0].equals("IO"))return null;
					PCB.instruction.add(new IOInstruction(Integer.parseInt(lines[1]),Integer.parseInt(lines[2])));
					break;
				default: return null;
				}
			}
			return null;
		}catch (Exception e) {
			return null;
		}
		
	}

	@Override
	public int getPID() {
		return PID;
	}

	@Override
	public String getProgramName() {
		return this.programName;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public int setPriority(int value) {
		int oldValue = this.priority;
		this.priority = value;
		return oldValue;
	}

	@Override
	public Instruction getInstruction() {
		return this.instruction.get(PC);
	}

	@Override
	public boolean hasNextInstruction() {
		return (this.PC+1 < this.instruction.size())?true: false;
	}

	@Override
	public void nextInstruction() {
		this.PC++;
		
	}

	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

}
