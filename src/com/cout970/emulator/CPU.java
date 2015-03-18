package com.cout970.emulator;

import java.util.Scanner;


public class CPU {

	public int regPC = 0;
	
	public int[] registes = new int[32];
	public float[] floatRegistes = new float[32];
	int HI = 0;
	int LO = 0;
	
	public byte[] memory = new byte[8192];
	
	public int cpuCicles = -1;

	public boolean waiting = false;;
	
	public int readWord(int pos){
		int dato = (readByte(pos) & 0xFF);
		dato |= (readByte(pos+1) & 0xFF) << 8;
		dato |= (readByte(pos+2) & 0xFF) << 16;
		dato |= (readByte(pos+3) & 0xFF) << 24;
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
		writeByte(pos  , (byte) (dato & 0x000000FF));
		writeByte(pos+1, (byte) ((dato & 0x0000FF00) >> 8));
		writeByte(pos+2, (byte) ((dato & 0x00FF0000) >> 16));
		writeByte(pos+3, (byte) ((dato & 0xFF000000) >> 24));
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
		regPC = (regPC + 4) % 0x2000;
	}

	public void executeInsntruction() {
		
		int instruct = readWord(regPC);
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
		case 3:
			SysCall();
		}
	}
	
	

	public int CONTROL(int instruct) {
		if(instruct == 0)return -1;
		if(instruct == 0x0000000c)return 3;//syscall
		int opcode = ((instruct & 0xFC000000) >> 26);
		if(opcode == 0)return 0;
		if(opcode >= 0x2 && opcode <= 0x7)return 1;
		
		return 2;
	}
	
	public void SysCall() {
		int t = getRegister(2);
		switch(t){
		case 10:
			cpuCicles = -1;
			break;
		case 1:
			System.out.println(getRegister(4));
			break;
		case 2:
			System.out.println(floatRegistes[12]);
			break;
		case 3:
			double dou;
			int aux1 = Float.floatToIntBits(floatRegistes[12]);
			int aux2 = Float.floatToIntBits(floatRegistes[13]);
			dou = (aux1 | (aux2 << 32));
			System.out.println(dou);
			break;
		case 4:
			int dir = getRegister(4);
			char[] string = new char[140];
			for(int i=0;i<140;i++){
				string[i] = (char)readByte(dir+i);
				if(string[i] == '\0')break;
			}
			System.out.println(string);
			break;
		case 5:
			Scanner in = new Scanner(System.in);
			setRegister(2, in.nextInt());
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
			String s = in111.next();
			for(int i=0;i<s.length();i++)
				memory[2048+i]= (byte) s.charAt(i);
			memory[2048+s.length()]= '\0';
			break;
		}
	}

	private void TipeI(int instruct) {
		int target,source,data,code;
		long aux = 0,aux2 = 0;
		code = (instruct & 0xFC000000) >> 26;
		target = (instruct & 0x1F0000) >> 16;
		source = (instruct & 0x3E00000) >> 21;
		data = instruct & 0xFFFF;
		switch(code){
		case 0x8://addi
			aux = data + getRegister(source); 
			setRegister(target, (int) aux);
			break;
		case 0x9://addiu
			aux |= getRegister(source);
			aux2 = data;
			setRegister(target , (int)(aux + aux2));
			break;
		case 0xc://andi
			aux = getRegister(target) & data;
			setRegister(target , (int)aux);
			break;
		case 0xd://ori
			aux = getRegister(target) | data;
			setRegister(target , (int)aux);
			break;
		case 0xe://xori
			aux = getRegister(target) ^ data;
			setRegister(target , (int)aux);
			break;
		}
	}

	public void TipeJ(int instruct) {
		int dir = instruct & 0x3FFFFFF;
		int code = (instruct & 0xFC000000) >> 26;
		switch(code){
		case 0x2://j
			regPC += dir << 2;
			break;
		case 0x3://jal
			setRegister(31, regPC);
			regPC += dir << 2;
			break;
		case 0x4://beq
			
			break;
		case 0x5://bne
			break;
		case 0x6://blez
			break;
		case 0x7://bgtz
			break;
		}
	}

	public void TipeR(int instruct) {
		int target,b,a,funct,desp;
		long aux = 0, aux2 = 0, aux3 = 0;
		funct = instruct & 0x3F;
		desp = (instruct & 0x7C0) >> 6;
		target = (instruct & 0xF800) >> 11;
		b = (instruct & 0x1F0000) >> 16;
		a = (instruct & 0x3E00000) >> 21;

		switch(funct){
		case 0x0://sll
			setRegister(target, a << desp);
			break;
		case 0x2://srl
			setRegister(target, a >>> desp);
			break;
		case 0x3://sra
			setRegister(target, a >> desp);
			break;
		case 0x4://sllv
			setRegister(target, a << b);
			break;
		case 0x6://srlv
			setRegister(target, a >>> b);
			break;
		case 0x7://srav
			setRegister(target, a >> b);
			break;
		case 0x8://jr
			//TODO
			regPC &= 0xF0000000;
			regPC |= (instruct & 0x3FFFFFF) << 2;
			break;
		case 0x9://jalr
			//TODO
			setRegister(31, regPC);
			regPC &= 0xF0000000;
			regPC |= (instruct & 0x3FFFFFF) << 2;
			break;
		case 0x10://mfhi
			setRegister(target, HI);
			break;
		case 0x11://mthi
			HI = getRegister(target);
			break;
		case 0x12://mflo
			setRegister(target, LO);
			break;
		case 0x13://mflo
			LO = getRegister(target);
			break;
		case 0x18://mult
			aux = getRegister(b) * getRegister(a);
			setRegister(LO, (int)(aux & -1));
			setRegister(HI, (int)((aux >> 32) & -1));
			break;
		case 0x19://multu
			aux2 |= getRegister(b);
			aux3 |= getRegister(a);
			aux = aux2 * aux3;
			setRegister(LO, (int)(aux & -1));
			setRegister(HI, (int)((aux >> 32) & -1));
			break;
		case 0x1a://div
			LO = getRegister(a) / getRegister(b);
			HI = getRegister(a) % getRegister(b);
			break;
		case 0x1b://divu
			aux2 |= getRegister(a);
			aux3 |= getRegister(b);
			LO = (int) (aux2 / aux3);
			HI = (int) (aux2 % aux3);
			break;	
		case 0x20://add
			setRegister(target, getRegister(b) + getRegister(a));
			break;
		case 0x21://addu
			aux |= getRegister(b);
			aux2 |= getRegister(a);
			setRegister(target , (int) (aux + aux2));
			break;
		case 0x22://sub
			setRegister(target , getRegister(a) - getRegister(b));
			break;
		case 0x23://subu
			aux |= getRegister(a);
			aux2 |= getRegister(b);
			setRegister(target , (int) (aux - aux2));
			break;
		case 0x24://and
			setRegister(target , getRegister(b) & getRegister(a));
			break;
		case 0x25://or
			setRegister(target , getRegister(b) | getRegister(a));
			break;
		case 0x26://xor
			setRegister(target , getRegister(b) ^ getRegister(a));
			break;
		case 0x27://nor
			setRegister(target , ~(getRegister(b) | getRegister(a)));
			break;
		case 0x2a://slt
			if(getRegister(a) < getRegister(b))
				setRegister(target, 1);
			else
				setRegister(target, 0);
			break;
		case 0x2b://sltu
			aux |= getRegister(a);
			aux2 |= getRegister(b);
			if(aux < aux2)
				setRegister(target, 1);
			else
				setRegister(target, 0);
			break;
		}
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
		writeWord(0, 0x20040012);
		writeWord(4, 0x20020001);
		writeWord(8, 0x0000000c);
		writeWord(12, 0x00000000);
		writeWord(16, 0x2002000a);
		writeWord(20, 0x0000000c);
	}

}
