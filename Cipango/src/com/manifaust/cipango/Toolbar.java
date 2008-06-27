package com.manifaust.cipango;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class Toolbar extends JPanel {
	private JButton openButton;

	public Toolbar() {
		super(new MigLayout());
		openButton = new JButton("Open a Folder...");

		this.add(openButton, "wrap");
	}
	
	public void addOpenButtonListener(ActionListener al) {
		openButton.addActionListener(al);
	}
		
}
