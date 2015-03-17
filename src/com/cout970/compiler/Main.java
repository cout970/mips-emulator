package com.cout970.compiler;

import com.cout970.compiler.Instruction.InstrucType;
import com.cout970.emulator.CPU;

public class Main {

	public static void main(String[] args){
		registerInstructions();
		CPU main = new CPU();
		main.startPC();
		while(true){
			main.advance();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void registerInstructions() {
		new Instruction("sll",0x0,InstrucType.R);
		new Instruction("srl",0x2,InstrucType.R);
		new Instruction("sra",0x3,InstrucType.R);
		new Instruction("sllv",0x4,InstrucType.R);
		new Instruction("srlv",0x6,InstrucType.R);
		new Instruction("srav",0x7,InstrucType.R);
		new Instruction("addi",0x8,InstrucType.I);
		new Instruction("addiu",0x9,InstrucType.I);
		new Instruction("andi",0xc,InstrucType.I);
		new Instruction("ori",0xd,InstrucType.I);
		new Instruction("xori",0xe,InstrucType.I);
		new Instruction("mult",0x18,InstrucType.R);
		new Instruction("multu",0x19,InstrucType.R);
		new Instruction("div",0x1a,InstrucType.R);
		new Instruction("divu",0x1b,InstrucType.R);
		new Instruction("add",0x20,InstrucType.R);
		new Instruction("addu",0x21,InstrucType.R);
		new Instruction("sub",0x22,InstrucType.R);
		new Instruction("subu",0x23,InstrucType.R);
		new Instruction("and",0x24,InstrucType.R);
		new Instruction("or",0x25,InstrucType.R);
		new Instruction("xor",0x26,InstrucType.R);
		new Instruction("nor",0x27,InstrucType.R);
	}
}
