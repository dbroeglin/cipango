package com.manifaust.cipango;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.vdheide.mp3.*;

public class Cipango extends JPanel implements ActionListener {
	private static final long serialVersionUID = -6145211126923439676L;
	
	JButton openButton;
	JTextArea infoArea;
	JFileChooser fc;
	JTree fsTree;
	
	ID3 id3;
	ID3v2 id3v2;

	public static void main(String[] args) {
		JFrame frame = new JFrame("Cipango v0.1");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new Cipango());
		frame.pack();
		frame.setVisible(true);
	}

	public Cipango() {
		super(new BorderLayout());
		
		infoArea = new JTextArea(20, 40);
		infoArea.setEditable(false);
		
		File homeDir = new File(System.getProperty("user.home"));
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(homeDir);
		populate(homeDir, rootNode);
		fsTree = new JTree(rootNode);
	    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	    renderer.setLeafIcon(UIManager.getIcon("Tree.closedIcon"));
	    fsTree.setCellRenderer(renderer);
		
		JScrollPane fsScrollPane = new JScrollPane(fsTree);
		JScrollPane infoScrollPane = new JScrollPane(infoArea);
		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, fsScrollPane, infoScrollPane);
		Dimension minimumSize = new Dimension(100, 50);
		fsScrollPane.setMinimumSize(minimumSize);
		infoScrollPane.setMinimumSize(minimumSize);
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		openButton = new JButton("Open a Folder...");
		openButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);
		
		add(buttonPanel, BorderLayout.PAGE_START);
		add(splitPane, BorderLayout.CENTER);

		String home = System.getProperty("user.home");
		System.out.println("user.home = " + home); 
	}
	
	private void populate(File topDir, DefaultMutableTreeNode topNode) {
		DefaultMutableTreeNode tempNode;
		DefaultMutableTreeNode dummyNode;
		AllowDirectoriesFilter dirPassFilter = new AllowDirectoriesFilter();
		
		// Allow users to only select folders
		String[] fileNameList = topDir.list(dirPassFilter);
		
		for(int i = 0; i < fileNameList.length; i++) {
			File tempFile = new File(topDir
					+ System.getProperty("file.separator") + fileNameList[i]);
			tempNode = new DefaultMutableTreeNode(tempFile);
			topNode.add(tempNode);
			// Add dummy nodes to folders that should look expandable
			if (tempFile.list(dirPassFilter).length != 0) {
				dummyNode = new DefaultMutableTreeNode("Dummy!");
				tempNode.add(dummyNode);
			}
		}
	}
	
	private class AllowDirectoriesFilter implements FilenameFilter {
		@Override
		public boolean accept(File fileDir, String fileName) {
			File file = new File(
					fileDir + System.getProperty("file.separator") + fileName);
			if (file.isDirectory() == true) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(Cipango.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				listTracks(file);
			} else {
				infoArea.append("Open command cancelled by user.\n");
			} 
		}
	}

	private void listTracks(File file) {
		File[] filelist = file.listFiles(new FilenameFilter() {
			private Pattern pattern = Pattern.compile("(.*)\\.mp3$");
			public boolean accept(File dir, String name) {
				Matcher m = pattern.matcher(name);
				if (m.matches())
					System.out.println(m.group(1));
				return m.matches();
			}
		});
		
		for (File mp3file : filelist) {
			try {
				id3 = new ID3(mp3file); // V1 tag
				id3.encoding = "big5-hkscs";
				id3v2 = new ID3v2(mp3file); // V2 tag

				boolean hasv1 = id3.checkForTag();
				boolean hasv2 = id3v2.hasTag();

				infoArea.append(mp3file.getName() + ":\n");
				if (hasv1) {
					try {
						infoArea.append("    v1 - ");
						infoArea.append("Artist: " + id3.getArtist() + ", ");
						infoArea.append("Title: " + id3.getTitle() + "\n");
					} catch (NoID3TagException e) {
						System.out.println("Error reading v1 tag of "
								+ mp3file.getName());
					}
				} else {
					System.out.println(mp3file.getName() + " has no v1 tag");
				}
				if (hasv2) {
					try {
						id3v2.getFrames();
						infoArea.append("    v2 - ");

						byte[] artistEncoded= ((ID3v2Frame)
								(id3v2.getFrame("TPE1").elementAt(0))).getContent();
						String artistDecoded = new String(artistEncoded, id3.encoding);
						byte[] titleEncoded= ((ID3v2Frame)
								(id3v2.getFrame("TIT2").elementAt(0))).getContent();
						String titleDecoded = new String(titleEncoded, id3.encoding);
						
						infoArea.append("Artist: " + artistDecoded + ", ");
						infoArea.append("Title: " + titleDecoded + "\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println(mp3file.getName() + " has no v2 tag");
				}
			} catch (ID3v2IllegalVersionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ID3v2WrongCRCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ID3v2DecompressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
