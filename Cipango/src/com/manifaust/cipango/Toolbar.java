package com.manifaust.cipango;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

class Toolbar extends JPanel {
	private JButton openButton;
	private JComboBox encodingCombo;

	public Toolbar() {
		super(new MigLayout());
		openButton = new JButton("Open a Folder...");
		encodingCombo = createEncodingCombo();

		this.add(openButton);
		this.add(encodingCombo);
	}
	
	public void addOpenButtonListener(ActionListener al) {
		openButton.addActionListener(al);
	}
	
	public JButton getOpenButton() {
		return openButton;
	}
	
	private JComboBox createEncodingCombo() {
		String[] encodings = {"UTF8", "UTF-16", "UnicodeBigUnmarked",
				"UnicodeLittleUnmarked", "Cp1252", "big5-hkscs", "shift_jis",
				"euc-jp", "gb18030", "gbk", "euc-kr"};
		JComboBox combo = new JComboBox(encodings);
		combo.setSelectedIndex(0);
		
		return combo;
	}
	
	public void addEncodingComboListener(ActionListener al) {
		encodingCombo.addActionListener(al);
	}
	
	public JComboBox getEncodingCombo() {
		return encodingCombo;
	}
	
	public String getEncoding() {
		return (String)encodingCombo.getSelectedItem();
	}
}
