package com.cout970.compiler;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Ventana {

	public static JFrame window;
	public static JTextArea text;
	public static JButton compilar;
	public static JButton script;
	
	public static void init(){
		window = new JFrame();
		window.setSize(500, 800);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setLayout(null);
		window.setResizable(false);
		
		text = new JTextArea();
		text.setBounds(10, 100, 470, 660);
		text.setVisible(true);
		window.add(text);
		
		compilar = new JButton("Compilar en Ensamblador");
		compilar.setBounds(15, 15, 200, 25);
		compilar.setVisible(true);
		compilar.addActionListener(new AssemblyCompiler());
		window.add(compilar);
		
		script = new JButton("Compilar en PepitoScript");
		script.setBounds(15, 55, 200, 25);
		script.setVisible(true);
		script.addActionListener(new ScriptCompiler());
		window.add(script);
	}
}
