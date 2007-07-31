package com.manifaust.cipango;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.farng.mp3.*;

/**
 * 
 */

/**
 * @author Tony Wong
 *
 */
public class Cipango extends JTextArea {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6145211126923439676L;

	/**
	 * @param args
	 */
	public Cipango() {
		File path = new File("C:\\Documents and Settings\\tonyw\\workspace\\" +
					"Cipango\\music\\MM Jukebox Plus Upgrade.mp3");
		MP3File song = null;
		try {
			song = new MP3File(path);
		} catch (IOException e) {
			throw new RuntimeException();
		} catch (TagException e) {
			throw new RuntimeException();
		}

		if (song.hasID3v1Tag() && song.hasID3v2Tag()) {
			append("Has both types of tags, " +
					"here's the v1 tag:\n" + song.getID3v1Tag() +
					"Here's the v2 tag:\n" + song.getID3v2Tag());
		} else if (song.hasID3v1Tag()) {
			append("Has v1 tag:\n" + song.getID3v1Tag());
		} else if (song.hasID3v2Tag()) {
			append("Has v2 tag:\n" + song.getID3v2Tag());
		} else {
			append("No tag found!");
		}
		
		OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
		append(out.getEncoding());
		
		append("\u00F6 \u4E9E 亞");
		System.out.println("\u00F6 \u4E9E 亞");
		
		SortedMap<String,Charset> charSets = Charset.availableCharsets();
		Iterator<String> it = charSets.keySet().iterator();
	    while(it.hasNext()) {
	    	String csName = it.next();
	    	System.out.print(csName);
	    	Iterator aliases = charSets.get(csName).aliases().iterator();
	    	if(aliases.hasNext())
	    		System.out.print(": ");
	    	while(aliases.hasNext()) {
	    		System.out.print(aliases.next());
		        if(aliases.hasNext())
		        	System.out.print(", ");
	    	}
	    	System.out.println();
	    }
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("Test");
		f.add(new JScrollPane(new Cipango()));
		f.setSize(400, 400);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
