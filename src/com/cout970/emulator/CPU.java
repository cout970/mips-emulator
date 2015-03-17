package com.cout970.emulator;

public class CPU {

	public int regPC = 0;
	
	public int[] registes = new int[34];
	int HI = 33;
	int LO = 32;
	
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
		}
	}
	
	public int CONTROL(int instruct) {
		if(instruct == 0)return -1;
		int opcode = ((instruct & -67108864) >> 26);
		if(opcode == 0)return 0;
		if(opcode == 0x2 || opcode == 0x3)return 1;
		if(instruct == 0x0000000c)return 3;//syscall
		return 2;
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
		int dir = instruct & 67108863;
		int code = (instruct & -67108864) >> 26;
		switch(code){
		case 0x2:
			regPC += dir << 2;
			break;
		case 0x3:
			setRegister(31, regPC);
			regPC += dir << 2;
			break;
		}
	}

	

	public void TipeR(int instruct) {
		int target,b,a,funct,desp, val;
		long aux = 0, aux2 = 0, aux3 = 0;
		funct = instruct & 0x3F;
		desp = (instruct & 0x7C0) >> 6;
		target = (instruct & 0xF800) >> 11;
		b = (instruct & 0x1F0000) >> 16;
		a = (instruct & 0x3E00000) >> 21;

		switch(funct){
		case 0x0:
			val = a << desp;
			setRegister(target, val);
			break;
		case 0x2:
			val = a >>> desp;
			setRegister(target, val);
			break;
		case 0x3:
			val = a >> desp;
			setRegister(target, val);
			break;
		case 0x4:
			val = a << b;
			setRegister(target, val);
			break;
		case 0x6:
			val = a >>> b;
			setRegister(target, val);
			break;
		case 0x7:
			val = a >> b;
			setRegister(target, val);
			break;
		case 0x8://jr
			regPC &= -268435456;
			regPC |= (instruct & 67108863) << 2;
			break;
		case 0x9:
			setRegister(31, regPC);
			regPC &= -268435456;
			regPC |= (instruct & 67108863) << 2;
			break;
			
		case 0x20://add
			val = getRegister(b) + getRegister(a);
			setRegister(target , val);
			break;
		case 0x21://addu
			aux |= getRegister(b);
			aux2 |= getRegister(a);
			val = (int) (aux + aux2);
			setRegister(target , val);
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
			val = getRegister(b) / getRegister(a);
			setRegister(LO, val);
			val = getRegister(b) % getRegister(a);
			setRegister(HI, val);
			break;
		case 0x1b://divu
			aux2 |= getRegister(b);
			aux3 |= getRegister(a);
			val = (int) (aux2 / aux3);
			setRegister(LO, val);
			val = (int) (aux2 % aux3);
			setRegister(HI, val);
			break;
		case 0x22://sub
			val = getRegister(b) - getRegister(a);
			setRegister(target , val);
			break;
		case 0x23://subu
			aux |= getRegister(b);
			aux2 |= getRegister(a);
			val = (int) (aux - aux2);
			setRegister(target , val);
			break;
		case 0x24://and
			val = getRegister(b) & getRegister(a);
			setRegister(target , val);
			break;
		case 0x25://or
			val = getRegister(b) | getRegister(a);
			setRegister(target , val);
			break;
		case 0x26://xor
			val = getRegister(b) ^ getRegister(a);
			setRegister(target , val);
			break;
		case 0x27://nor
			val = ~(getRegister(b) | getRegister(a));
			setRegister(target , val);
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
		writeWord(0, 0x20080032);
		writeWord(4, 0x21090078);
		writeWord(8, 0x01098020);
		writeWord(12, 0x2002000a);
//		writeWord(16, 0x0000000c);
//		writeWord(20, 0x00000000);
	}

}
