package com.cout970.compiler;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
	
	public static List<Instruction> orders = new ArrayList<Instruction>();

	public String name;
	public int code;
	public InstrucType tipe;
	public int argNumber;
	
	public Instruction(String name,int code,InstrucType t,int args){
		this.name = name;
		this.code = code;
		orders.add(this);
		tipe = t;
		argNumber = args;
	}
	
	public enum InstrucType{
		R,//codeop(6), rs(5), rt(5), rd(5), shamt(5), codefunc(6)
		I,//codeop(6), rs(5), rt(5), desplazamiento(16)
		J;//codeop(6), direccion(26)
	}
}
