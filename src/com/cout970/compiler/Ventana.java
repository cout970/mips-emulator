package com.cout970.compiler;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Ventana {

	public static JFrame window;
	
	public static void init(){
		window = new JFrame();
		window.setSize(500, 800);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setLayout(null);
		window.setResizable(false);
		
		JTextArea text = new JTextArea();
		text.setBounds(10, 100, 470, 660);
		text.setVisible(true);
		window.add(text);
	}
}
