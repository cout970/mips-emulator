package com.cout970.emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;


public class CPU {

	public int regPC = 0;
	
	public int[] registes = new int[32];
	public float[] floatRegistes = new float[32];
	int HI = 0;
	int LO = 0;
	
	public byte[] memory = new byte[8388608];//8Mb
	
	public int cpuCicles = -1;

	public boolean waiting = false;;
	
	public int readWord(int pos){
		int dato;
		dato  = (readByte(pos+3) & 0xFF);
		dato |= (readByte(pos+2) & 0xFF) << 8;
		dato |= (readByte(pos+1) & 0xFF) << 16;
		dato |= (readByte(pos  ) & 0xFF) << 24;
		return dato;
	}
	
	public byte readByte(int pos){
		if(pos < 0 || pos > memory.length)return 0;
		return memory[pos];
	}
	
	public void writeByte(int pos, byte dato){
		if(pos < 0 || pos > memory.length)return;
		memory[pos] = dato;
	}
	
	public void writeWord(int pos, int dato){
		writeByte(pos+3, (byte) (dato & 0x000000FF));
		writeByte(pos+2, (byte) ((dato & 0x0000FF00) >> 8));
		writeByte(pos+1, (byte) ((dato & 0x00FF0000) >> 16));
		writeByte(pos  , (byte) ((dato & 0xFF000000) >> 24));
	}
	
	public static int getBitsFromInstruct(int inst, int start, int end,  boolean signed) {
		if(start > end) {
			int temp = end;
			end = start;
			start = temp;
		}
		int max = 0xFFFFFFFF;
		int mask = (max >>> (31-end)) & (max << start);
		inst = inst & mask;
		if(signed) {
			inst = inst << (31 - end);
			return inst >> (start + (31-end));
		}
		return inst >>> start;
	}
	
	public boolean isRunning(){
		return cpuCicles >= 0;
	}
	
	public void advance(){
		if(cpuCicles >= 0){
			cpuCicles += 1000;
			
			if (cpuCicles > 100000){
				cpuCicles = 100000;
			}
			while (cpuCicles > 0 && !waiting )
			{
				--this.cpuCicles;
				this.executeInsntruction();
			}
		}else{
			System.exit(0);
		}
	}

	public void advancePC() {
		regPC = regPC + 4; 
		if(regPC >= memory.length)regPC = 1024;
	}

	public void executeInsntruction() {
		
		int instruct = readWord(regPC);
//		System.out.println(Integer.toHexString(instruct).toUpperCase());
		advancePC();
		switch(CONTROL(instruct)){
		case 0:
			TipeR(instruct);
			break;
		case 1:
			TipeJ(instruct);
			break;
		case 2:
			TipeI(instruct);
			break;
		case 3:
			SysCall();
		}
	}

	public int CONTROL(int instruct) {
		if(instruct == 0)return -1;//no action
		if(instruct == 0x0000000c)return 3;//syscall
		int opcode = getBitsFromInstruct(instruct, 26, 31, false);
		if(opcode == 0)return 0;//ALU
		if(opcode == 0x2 || opcode == 0x3)return 1;//tipe j
		return 2;//tipe i
	}
	
	public void SysCall() {
		int t = getRegister(2);
		switch(t){
		case 10:
			cpuCicles = -1;
			break;
		case 1://print int
			System.out.println(getRegister(4));
			break;
		case 2://print float
			System.out.println(floatRegistes[12]);
			break;
		case 3://print double
			double dou;
			int aux1 = Float.floatToIntBits(floatRegistes[12]);
			int aux2 = Float.floatToIntBits(floatRegistes[13]);
			dou = (aux1 | (aux2 << 32));
			System.out.println(dou);
			break;
		case 4://print string
			int dir = getRegister(4);
			boolean stop = false;
			while(!stop){
				byte c = readByte(dir);
				if(c == 0){
					stop = true;
					break;
				}
				System.out.print((char)(c));
				dir++;
			}
			break;
		case 5://read string
			Scanner in = new Scanner(System.in);
			String input = in.nextLine();
			int temp = 0;
			try{
				temp = Integer.parseInt(input);
				setRegister(2, temp);
			}catch(NumberFormatException e){
				System.out.println("Invalid input");
			}
			break;
		case 6:
			Scanner in1 = new Scanner(System.in);
			floatRegistes[12] = in1.nextFloat();
			break;
		case 7:
			Scanner in11 = new Scanner(System.in);
			double d = in11.nextDouble();
			long aux = Double.doubleToRawLongBits(d);
			floatRegistes[12] = (aux & 0xFFFFFFFF);
			floatRegistes[13] = (aux & 0xFFFFFFFF00000000L);
			break;
		case 8:
			Scanner in111 = new Scanner(System.in);
			String s = in111.nextLine();
			for(int i=0;i<s.length();i++)
				memory[getRegister(4)+i]= (byte) s.charAt(i);
			memory[getRegister(4)+s.length()]= '\0';
			break;
		}
	}

	private void TipeI(int instruct) {
		int opcode, rs, rt, inmed,inmedU;
		long m1 = 0,m2 = 0;
		
		opcode = getBitsFromInstruct(instruct, 26, 31, false);
		rs = getBitsFromInstruct(instruct, 21, 25, false);
		rt = getBitsFromInstruct(instruct, 16, 20, false);
		inmed = getBitsFromInstruct(instruct, 0, 15, true);
		inmedU = getBitsFromInstruct(instruct, 0, 15, false);
		
		switch(opcode){
		case 0x1:
			if(rt == 1){//bgez
				if(getRegister(rs) >= 0){
					regPC -= 4;
					regPC += (inmed << 2);
				}
			}else if(rt == 0){//bltz
				if(getRegister(rs) < 0){
					regPC -= 4;
					regPC += (inmed << 2);
				}
			}
			break;
		case 0x4://beq
			if(getRegister(rt) == getRegister(rs)){
//				regPC -= 4;
				regPC += (inmed << 2);
			}
			break;
		case 0x5://bne
			if(getRegister(rt) != getRegister(rs)){
//				regPC -= 4;
				regPC += (inmed << 2);
			}
			break;
		case 0x6://blez
			if(getRegister(rs) <= 0){
				regPC -= 4;
				regPC += (inmed << 2);
			}
			break;
		case 0x7://bgtz
			if(getRegister(rs) > 0){
				regPC -= 4;
				regPC += (inmed << 2);
			}
			break;
		case 0x8://addi
			setRegister(rt, getRegister(rs) + inmed);
			break;
		case 0x9://addiu
			setRegister(rt, getRegister(rs) + inmedU);
			break;
		case 0xa://slti
			if(getRegister(rs) < inmed)
				setRegister(rt, 1);
			else
				setRegister(rt, 0);
			break;
		case 0xb://sltiu
			m1 = getRegister(rs);
			m2 = inmed;
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			if(m1 < m2)
				setRegister(rt, 1);
			else
				setRegister(rt, 0);
			break;
		case 0xc://andi
			setRegister(rt , getRegister(rs) & inmedU);
			break;
		case 0xd://ori
			setRegister(rt , getRegister(rs) | inmedU);
			break;
		case 0xe://xori
			setRegister(rt , getRegister(rs) ^ inmedU);
			break;
		case 0xf://lui
			setRegister(rt, inmedU << 16);
			break;
		case 0x18://llo
			setRegister(rt, (getRegister(rt) & 0xFFFF0000) | inmedU);
			break;
		case 0x19://lhi
			setRegister(rt, (getRegister(rt) & 0x0000FFFF) | (inmedU << 16));
			break;
			
		case 0x1a://trap
			SysCall();//no exactly but good for now
			break;
			
		case 0x20://lb
			setRegister(rt, readByte(getRegister(rs)+inmed));
			break;
		case 0x21://lh
			setRegister(rt, (short)(readWord(getRegister(rs)+inmed)));
			break;
		case 0x23://lw
			setRegister(rt, readWord(getRegister(rs)+inmed));
			break;
		case 0x24://lbu
			setRegister(rt, readByte(getRegister(rs)+inmed) & 0xFF);
			break;
		case 0x25://lhu
			setRegister(rt, readWord(getRegister(rs)+inmed) & 0xFFFF);
			break;
		case 0x28://sb
			writeByte(getRegister(rs)+inmed, (byte)(getRegister(rt) & 0xFF));
			break;
		case 0x29://sh
			writeByte(getRegister(rs)+inmed, (byte)(getRegister(rt) & 0xFF));
			writeByte(getRegister(rs)+inmed+1, (byte)(getRegister(rt) & 0xFF00));
			break;
		case 0x2b://sw
			writeWord(getRegister(rs)+inmed, getRegister(rt));
			break;
		}
	}

	public void TipeJ(int instruct) {
		int dir = getBitsFromInstruct(instruct, 0, 25, false);
		int code = getBitsFromInstruct(instruct, 26, 31, false);
		
		switch(code){
		case 0x2://j
			regPC -= 4;
			regPC &= 0xF0000000;
			regPC |= dir << 2;
			break;
		case 0x3://jal
			setRegister(31, regPC);
			regPC -= 4;
			regPC &= 0xF0000000;
			regPC |= dir << 2;
			break;
		}
	}

	public void TipeR(int instruct) {
		int rs,rt,rd,shamt,func;
		long m1,m2,mt;
		
		func = getBitsFromInstruct(instruct, 0, 5, false);
		shamt = getBitsFromInstruct(instruct, 6, 10, false);
		rd = getBitsFromInstruct(instruct, 11, 15, false);
		rt = getBitsFromInstruct(instruct, 16, 20, false);
		rs = getBitsFromInstruct(instruct, 21, 25, false);

		switch(func){
		
		case 0x0://sll
			setRegister(rd, rt << shamt);
			break;
		case 0x2://srl
			setRegister(rd, rt >>> shamt);
			break;
		case 0x3://sra
			setRegister(rd, rt >>> shamt);
			break;
		case 0x4://sllv
			setRegister(rd, rt << rs);
			break;
		case 0x6://srlv
			setRegister(rd, rt >> rs);
			break;
		case 0x7://srav
			setRegister(rd, rs >>> rt);
			break;
			
		case 0x8://jr
			if(getRegister(rs) == -1)return;
			regPC = getRegister(rs);
			break;
		case 0x9://jalr
			if(getRegister(rs) == -1)return;
			setRegister(31, regPC);
			regPC = getRegister(rs);
			break;
			
		case 0x10://mfhi
			setRegister(rd, HI);
			break;
		case 0x11://mthi
			HI = getRegister(rd);
			break;
		case 0x12://mflo
			setRegister(rd, LO);
			break;
		case 0x13://mflo
			LO = getRegister(rd);
			break;
			
		case 0x18://mult
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			mt = m1 * m2;
			LO = (int)mt;
			HI = (int)(mt >> 32);
			break;
		case 0x19://multu
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			mt = m1 * m2;
			LO = (int)mt;
			HI = (int)(mt >> 32);
			break;
		case 0x1a://div
			if(getRegister(rt) != 0) LO = getRegister(rs) / getRegister(rt);
			else LO = 0;
			HI = getRegister(rs) % getRegister(rt);
			break;
		case 0x1b://divu
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			LO = (int) (m1 / m2);
			HI = (int) (m1 % m2);
			break;	
		case 0x20://add
			setRegister(rd, getRegister(rt) + getRegister(rs));
			break;
		case 0x21://addu
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			mt = m1 + m2;
			setRegister(rd , (int) mt);
			break;
		case 0x22://sub
			setRegister(rd , getRegister(rs) - getRegister(rt));
			break;
		case 0x23://subu
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			mt = m1 - m2;
			setRegister(rd , (int) mt);
			break;
		case 0x24://and
			setRegister(rd , getRegister(rt) & getRegister(rs));
			break;
		case 0x25://or
			setRegister(rd , getRegister(rt) | getRegister(rs));
			break;
		case 0x26://xor
			setRegister(rd , getRegister(rt) ^ getRegister(rs));
			break;
		case 0x27://nor
			setRegister(rd , ~(getRegister(rt) | getRegister(rs)));
			break;
			
		case 0x2a://slt
			if(getRegister(rs) < getRegister(rt))
				setRegister(rd, 1);
			else
				setRegister(rd, 0);
			break;
		case 0x2b://sltu
			m1 = getRegister(rs);
			m2 = getRegister(rt);
			m1 = (m1 << 32) >>> 32;
			m2 = (m2 << 32) >>> 32;
			if(m1 < m2)
				setRegister(rd, 1);
			else
				setRegister(rd, 0);
			break;
		}
	}
	
	public int c2Converter(int value){
		return ~value + 1;
	}

	public void setRegister(int s, int val) {
		if(s==0)return;
		registes[s] = val;
	}

	public int getRegister(int t) {
		return registes[t];
	}

	public void startPC() {
		cpuCicles = 0;
		regPC = 4194304;
		boolean load = true;
		if(load){
			loadOS();
		}
		int pos = 0x00000000;
		writeWord(pos, 0x00000001);pos+=4;
		writeWord(pos, 0x00000005);pos+=4;
		writeWord(pos, 0x00000002);pos+=4;
		writeWord(pos, 0x00000008);pos+=4;
		writeWord(pos, 0x00000006);pos+=4;
		writeWord(pos, 0x00000003);pos+=4;
		writeWord(pos, 0x00000006);pos+=4;
		writeWord(pos, 0x00000000);pos+=4;
		writeWord(pos, 0x00000000);pos+=4;
	}

	private boolean loadOS() {
		File archive;
		FileInputStream stream = null;
		try{
			archive = new File("I:/Development/Compilador assembler/res/OperativeSystem.bin");
			System.out.println(archive.getPath());
			stream = new FileInputStream(archive);
			stream.read(memory, 4194304, 8192);
			stream.close();
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
