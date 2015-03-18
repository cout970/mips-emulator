package com.cout970.compiler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import com.cout970.compiler.Instruction.InstrucType;

public class AssemblyCompiler implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		List<Integer> program = new ArrayList<Integer>();
		List<String> toCode = new ArrayList<String>();
		
		String code = Ventana.text.getText();
		String[] subcodes = code.split('\13'+"");
		String[] aux,aux2;
		String line;
		int instr = 0;
		
		for(int l = 0; l<subcodes.length; l++){
			line = subcodes[l];
			line = line.trim();
			aux = line.split(" ");
			for(int i=0;i<aux.length;i++){
				aux2 = aux[i].split(",");
				for(int j=0;j<aux2.length;j++){
					toCode.add(aux2[j]);
				}
			}
			instr = procesLine(toCode,l);
			program.add(Integer.valueOf(instr));
			toCode.clear();
			instr = 0;
		}
		for(Integer i : program){
			System.out.println(Integer.toHexString(i));
		}
	}

	private int procesLine(List<String> toCode, int l) {
		Instruction instr;
		int HexCode = 0;
		int d,s,t;
		instr = getInstruction(toCode.get(0));
		if(instr == null){
			System.out.println("Error compiling at "+l+" line");
			System.out.println("Invalid operation: "+toCode.get(0));
			return 0;
		}
		if(instr.tipe == InstrucType.R){
			d = Integer.parseInt(toCode.get(1));
			s = Integer.parseInt(toCode.get(2));
			t = Integer.parseInt(toCode.get(3));
			HexCode |= (d & 0x1F) << 11;
			HexCode |= (t & 0x1F << 16);
			HexCode |= (s & 0x1F << 21);
			
		}else if(instr.tipe == InstrucType.I){
			t = getCodeReg(toCode.get(1));
			s = getCodeReg(toCode.get(2));
			d = getCodeReg(toCode.get(3));
			HexCode |= (instr.code << 26);
			HexCode |= (d & 0xFFFF);
			HexCode |= (t & 0x1F) << 16;
			HexCode |= (s & 0x1F) << 21;
			
		}else{
			HexCode |= (instr.code << 26);
			HexCode |= Integer.parseInt(toCode.get(1)) & 0x3FFFFFF;
		}
		return HexCode;
	}

	private int getCodeReg(String string) {
		String format = string.replace('$'+"", "");
		int reg = 0;
		try{
			reg = Integer.parseInt(format);
		}catch(Exception e){
			switch(format.charAt(0)){
			case 'z':
				reg = 0;
				break;
			case 'a':
				if(format.charAt(1) == 't')reg = 1;
				else reg = 4 + Integer.parseInt(format.charAt(1)+"");
				break;
			case 'v':
				reg = 2 + Integer.parseInt(format.charAt(1)+"");
				break;
			case 't':
				reg = Integer.parseInt(format.charAt(1)+"");
				if(reg <= 7)reg = reg + 8;
				else reg = reg + 24;
				break;
			case 's':
				if(format.charAt(1) == 'p')reg = 29;
				else reg = 16 + Integer.parseInt(format.charAt(1)+"");
				break;
			case 'k':
				if(format.charAt(1) == '0')reg = 26;
				else reg = 27;
				break;
			case 'g':
				reg = 28;
				break;
			case 'r':
				reg = 31;
				break;
			}
		}
		return reg;
	}

	private Instruction getInstruction(String string) {
		for(Instruction i : Instruction.orders){
			if(i.name.equalsIgnoreCase(string))return i;
		}
		return null;
	}
}
