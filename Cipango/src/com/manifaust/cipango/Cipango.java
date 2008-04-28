package com.manifaust.cipango;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;

import net.miginfocom.swing.MigLayout;

import de.vdheide.mp3.*;

public class Cipango extends JFrame implements ActionListener,
		TreeWillExpandListener, TreeSelectionListener {
	JFrame frame;
	JButton openButton;
	private JPanel infoArea; // top-right
	JFileChooser fc;
	JTree fsTree;
	private JTable mp3Table = new JTable(new DefaultTableModel());
	private Vector columnNames = new Vector(Arrays.asList(
			"File Name", "Artist v1", "Title v1", "Artist v2", "Title v2"));
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		JFrame frame = new Cipango();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public Cipango() {
		super("Cipango v0.2");
		
		File homeDir = new File(System.getProperty("user.home"));
		MyNode rootNode = new MyNode(homeDir);
		populate(homeDir, rootNode);
		fsTree = new JTree(rootNode);
	    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	    renderer.setLeafIcon(UIManager.getIcon("Tree.closedIcon"));
	    fsTree.setCellRenderer(renderer);
	    fsTree.addTreeWillExpandListener(this);
	    fsTree.addTreeSelectionListener(this);
		
		JScrollPane fsScrollPane = new JScrollPane(fsTree);
		JSplitPane fileInfoPane = createFileInfoPane();
		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, fsScrollPane, fileInfoPane);
		fsScrollPane.setPreferredSize(new Dimension(200, 300));
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		openButton = new JButton("Open a Folder...");
		openButton.addActionListener(this);

		JPanel toolBar = new JPanel(new MigLayout());
		toolBar.add(openButton, "wrap");
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(toolBar);
		getContentPane().add(splitPane);
	}
	
	// creates right-top and right-bottom panes
	private JSplitPane createFileInfoPane() {
		infoArea = new JPanel(new BorderLayout());
		JPanel encodedTagsPanel = new JPanel(new MigLayout());
		
		((DefaultTableModel)(mp3Table.getModel())).setColumnIdentifiers(columnNames);
		mp3Table.setFillsViewportHeight(true);
		mp3Table.setFont(new Font("Default", Font.PLAIN, 17));
		mp3Table.setRowHeight(25);
		infoArea.add(new JScrollPane(mp3Table));
		
		JSplitPane splitPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, infoArea, encodedTagsPanel);
		infoArea.setPreferredSize(new Dimension(500, 300));
		encodedTagsPanel.setPreferredSize(new Dimension(500, 200));
		return splitPane;
	}

	/**
	 * This method takes a directory and the node associated with that directory
	 * and adds all its children folders to that node. For those child
	 * directories that have folders underneath them, this method will add
	 * dummy nodes to make the child directories look expandable.
	 * 
	 * @param topDir The folder with child directories to be added
	 * @param topNode The node associated with topDir
	 */
	private void populate(File topDir, MyNode topNode) {
		MyNode tempNode;
		MyNode dummyNode;
		AllowDirectoriesFilter dirPassFilter = new AllowDirectoriesFilter();
		
		// Get rid of the dummy node at current level
		topNode.removeAllChildren();
		
		// Allow users to only select folders
		String[] fileNameList = topDir.list(dirPassFilter);
		
		for(int i = 0; i < fileNameList.length; i++) {
			File tempFile = new File(topDir
					+ System.getProperty("file.separator") + fileNameList[i]);
			tempNode = new MyNode(tempFile);
			topNode.add(tempNode);
			// Add dummy nodes to child directories that should look expandable
			if (tempFile.list(dirPassFilter).length != 0) {
				dummyNode = new MyNode("Dummy!");
				tempNode.add(dummyNode);
			}
		}
	}
	
	/**
	 * This filter will only allow directories to pass.
	 */
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

	/**
	 * Detects "Open Folder" button press.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openButton) {
			int returnVal = fc.showOpenDialog(Cipango.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				listTracks(file);
			}
		}
	}

	/**
	 * Outputs the ID3v1 and ID3v2 information of all mp3 files in a folder.
	 * Modifies the DefaultTableModel of the JTable stored in infoArea JPanel.
	 * 
	 * @param file The directory containing mp3 files 
	 */
	private void listTracks(File file) {
		// find files with mp3 file extension
		File[] filelist = file.listFiles(new FilenameFilter() {
			private Pattern pattern = Pattern.compile("(.*)\\.mp3$");
			public boolean accept(File dir, String name) {
				Matcher m = pattern.matcher(name);
				if (m.matches())
					System.out.println(m.group(1));
				return m.matches();
			}
		});
		
		Vector rowData = new Vector();
		ID3 id3;
		ID3v2 id3v2;

		// extract id3 information to replace mp3table's model
		for (int i = 0; i < filelist.length; i++) {
			File mp3file = filelist[i];
			try {
				id3 = new ID3(mp3file); // V1 tag
				id3.encoding = "big5-hkscs";
				id3v2 = new ID3v2(mp3file); // V2 tag

				boolean hasv1 = id3.checkForTag();
				boolean hasv2 = id3v2.hasTag();

				Vector row = new Vector();
				row.add(mp3file.getName());
				
				// check if there are id3v1 tags
				if (hasv1) {
					try {
						row.add(id3.getArtist());
						row.add(id3.getTitle());
					} catch (NoID3TagException e) {
						System.out.println("Error reading v1 tag of "
								+ mp3file.getName());
					}
				} else {
					System.out.println(mp3file.getName() + " has no v1 tag");
				}
				
				// check if there are id3v2 tags
				if (hasv2) {
					try {
						id3v2.getFrames();

						byte[] artistEncoded= ((ID3v2Frame)
								(id3v2.getFrame("TPE1").elementAt(0))).getContent();
						String artistDecoded = new String(artistEncoded, id3.encoding);
						byte[] titleEncoded= ((ID3v2Frame)
								(id3v2.getFrame("TIT2").elementAt(0))).getContent();
						String titleDecoded = new String(titleEncoded, id3.encoding);
						
						row.add(artistDecoded);
						row.add(titleDecoded);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println(mp3file.getName() + " has no v2 tag");
				}
				rowData.add(row);
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
			((DefaultTableModel)mp3Table.getModel()).setDataVector(rowData, columnNames);
		}
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent arg0)
			throws ExpandVetoException {
		// Always allow collapse
	}

	/**
	 * Finds the node/directory being expanded, and loads its children onto
	 * the tree.
	 */
	@Override
	public void treeWillExpand(TreeExpansionEvent tee)
			throws ExpandVetoException {
		MyNode expandingNode =
			(MyNode)tee.getPath().getLastPathComponent();
		File expandingDir = (File)expandingNode.getUserObject();
		// Load children directories onto tree
		populate(expandingDir, expandingNode);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		MyNode node = (MyNode)fsTree.getLastSelectedPathComponent();
		
		if (node == null) {
			return;
		}
		
		listTracks((File)node.getUserObject());
	}
	
	
}

class MyNode extends DefaultMutableTreeNode {
	MyNode(File f) {
		super(f);
	}
	
	MyNode(String s) {
		super(s);
	}
	
	public String toString() {
		File f = (File)this.getUserObject();
		return f.getName();
	}
}