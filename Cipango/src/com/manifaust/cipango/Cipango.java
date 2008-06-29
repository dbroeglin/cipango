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

public class Cipango extends JFrame implements ActionListener, TreeSelectionListener {
	private JFrame frame;
	private JPanel infoArea; // top-right
	private JFileChooser fc;
	private JTree fsTree = createFsTree();
	private JTable mp3Table = new JTable(new DefaultTableModel());
	private Vector columnNames = new Vector(Arrays.asList(
			"File Name", "Artist v1", "Title v1", "Artist v2", "Title v2"));
	private Toolbar toolbar = new Toolbar();
	private File curDir;
	
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
		super("Cipango v0.3");
		
//	    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//	    renderer.setLeafIcon(UIManager.getIcon("Tree.closedIcon"));
//	    fsTree.setCellRenderer(renderer);
	    fsTree.addTreeSelectionListener(this);
		
		JScrollPane fsScrollPane = new JScrollPane(fsTree);
		JSplitPane fileInfoPane = createFileInfoPane();
		JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, fsScrollPane, fileInfoPane);
		fsScrollPane.setPreferredSize(new Dimension(200, 300));
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(toolbar);
		toolbar.addOpenButtonListener(this);
		toolbar.addEncodingComboListener(this);
		getContentPane().add(splitPane);
	}
	
	private JTree createFsTree() {
		File root = new File(System.getProperty("user.home"));
	    FileTreeModel model = new FileTreeModel(root);
	    JTree tree = new JTree();
	    tree.setModel(model);
	    
	    return tree;
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
	 * Detects "Open Folder" button press.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == toolbar.getOpenButton()) {
			int returnVal = fc.showOpenDialog(Cipango.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				setCurDir(file);
				listTracks();
			}
		} else if (e.getSource() == toolbar.getEncodingCombo()) {
			listTracks();
		}
	}

	/**
	 * Outputs the ID3v1 and ID3v2 information of all mp3 files in a folder.
	 * Modifies the DefaultTableModel of the JTable stored in infoArea JPanel.
	 * 
	 * @param file The directory containing mp3 files 
	 */
	private void listTracks() {
		// find files with mp3 file extension
		File[] filelist = getCurDir().listFiles(new FilenameFilter() {
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
		for (int i = 0; filelist != null && i < filelist.length; i++) {
			File mp3file = filelist[i];
			try {
				id3 = new ID3(mp3file); // V1 tag
				id3.encoding = toolbar.getEncoding();
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

	/**
	 * method for TreeSelectionListener implementation
	 * update current directory and refresh track listing
	 */
	public void valueChanged(TreeSelectionEvent e) {
		setCurDir((File)fsTree.getLastSelectedPathComponent());
		listTracks();
	}

	public File getCurDir() {
		return curDir;
	}

	public void setCurDir(File curDir) {
		this.curDir = curDir;
	}
	
}

/**
 * The methods in this class allow the JTree component to traverse
 * the file system tree, and display the files and directories.
 **/
class FileTreeModel implements TreeModel {
	// We specify the root directory when we create the model.
	protected File root;
	AllowDirectoriesFilter dirFilter = new AllowDirectoriesFilter();
	
	public FileTreeModel(File root) { this.root = root; }

	// The model knows how to return the root object of the tree
	public Object getRoot() { return root; }

	// TODO: implement better leaf folder view
	// Tell JTree whether an object in the tree is a leaf or not
	public boolean isLeaf(Object node) {  return ((File)node).isFile(); }

	// Tell JTree how many children a node has
	public int getChildCount(Object parent) {
		String[] children = ((File)parent).list(dirFilter);
		if (children == null) return 0;
		return children.length;
	}

	// Fetch any numbered child of a node for the JTree.
	// Our model returns File objects for all nodes in the tree.  The
	// JTree displays these by calling the File.toString() method.
	public Object getChild(Object parent, int index) {
		String[] children = ((File)parent).list(dirFilter);
		if ((children == null) || (index >= children.length)) return null;
		return new File((File) parent, children[index]);
	}

	// Figure out a child's position in its parent node.
	public int getIndexOfChild(Object parent, Object child) {
		String[] children = ((File)parent).list(dirFilter);
		if (children == null) return -1;
		String childname = ((File)child).getName();
		for(int i = 0; i < children.length; i++) {
			if (childname.equals(children[i])) return i;
		}
		return -1;
	}

	// This method is only invoked by the JTree for editable trees.  
	// This TreeModel does not allow editing, so we do not implement 
	// this method.  The JTree editable property is false by default.
	public void valueForPathChanged(TreePath path, Object newvalue) {}

	// Since this is not an editable tree model, we never fire any events,
	// so we don't actually have to keep track of interested listeners.
	public void addTreeModelListener(TreeModelListener l) {}
	public void removeTreeModelListener(TreeModelListener l) {}
}

/**
 * This filter will only allow directories to pass.
 */
class AllowDirectoriesFilter implements FilenameFilter {
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
