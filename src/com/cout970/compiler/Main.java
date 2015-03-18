package com.cout970.compiler;

import com.cout970.compiler.Instruction.InstrucType;
import com.cout970.emulator.CPU;

public class Main {

	public static void main(String[] args){
		registerInstructions();
		Ventana.init();
//		CPU main = new CPU();
//		main.startPC();
//		while(true){
//			main.advance();
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}

	private static void registerInstructions() {
		//opcode == 0
		new Instruction("sll",0x0,InstrucType.R,3);
		new Instruction("srl",0x2,InstrucType.R,3);
		new Instruction("sra",0x3,InstrucType.R,3);
		new Instruction("sllv",0x4,InstrucType.R,3);
		new Instruction("srlv",0x6,InstrucType.R,3);
		new Instruction("srav",0x7,InstrucType.R,3);
		new Instruction("jr",0x8,InstrucType.R,1);
		new Instruction("jalr",0x9,InstrucType.R,1);
		new Instruction("mfhi",0xa,InstrucType.R,1);
		
		new Instruction("mult",0x18,InstrucType.R,3);
		new Instruction("multu",0x19,InstrucType.R,3);
		new Instruction("div",0x1a,InstrucType.R,3);
		new Instruction("divu",0x1b,InstrucType.R,3);
		new Instruction("add",0x20,InstrucType.R,3);
		new Instruction("addu",0x21,InstrucType.R,3);
		new Instruction("sub",0x22,InstrucType.R,3);
		new Instruction("subu",0x23,InstrucType.R,3);
		new Instruction("and",0x24,InstrucType.R,3);
		new Instruction("or",0x25,InstrucType.R,3);
		new Instruction("xor",0x26,InstrucType.R,3);
		new Instruction("nor",0x27,InstrucType.R,3);
		//opcode != 0
		new Instruction("addi",0x8,InstrucType.I,3);
		new Instruction("addiu",0x9,InstrucType.I,3);
		new Instruction("andi",0xc,InstrucType.I,3);
		new Instruction("ori",0xd,InstrucType.I,3);
		new Instruction("xori",0xe,InstrucType.I,3);
	}
}
