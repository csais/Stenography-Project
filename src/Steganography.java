import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Steganography extends JFrame implements KeyListener, Action {
	JTextArea textbox;
	JTextArea textbox2;
	JPanel encodepanel;
	JPanel decodepanel;
	JButton encodeButton, decodeButton;
	private Calculations calc;
	private BufferedImage image;
	private File filetodecode;

	public Steganography() {
		super("Image Steganography");

		//filetodecode should start as null
		filetodecode = null; 
		
		// creation of Calculation instance
		calc = new Calculations();
		// creation of menu with encode
		// decode and exit options
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");

		JMenuItem decode = new JMenuItem("Decode");
		JMenuItem encode = new JMenuItem("Encode");
		JMenuItem exit = new JMenuItem("Exit");

		// this is for encode panel
		textbox = new JTextArea();
		encodepanel = new JPanel();
		encodepanel.setLayout(new BorderLayout());
		encodeButton = new JButton("Select an image to encode this message in");
		JScrollPane scroll = new JScrollPane(textbox);

		encodepanel.add(scroll);
		JPanel button1 = new JPanel();
		button1.setLayout(new BorderLayout());
		button1.add(encodeButton, BorderLayout.SOUTH);
		encodepanel.add(encodeButton, BorderLayout.SOUTH);

		// this is for decode panel
		decodepanel = new JPanel();
		decodepanel.setLayout(new BorderLayout());
		decodeButton = new JButton("Click to Decode Image");

		JPanel button2 = new JPanel();
		button2.setLayout(new BorderLayout());
		button2.add(decodeButton, BorderLayout.SOUTH);
		decodepanel.add(decodeButton, BorderLayout.SOUTH);

		// upon clicking the encodebutton
		encodeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				File x = selectFile();
				// make a copy of selected File
				BufferedImage img1 = calc.convertToBufferedImage(x);
				BufferedImage img = calc.deepCopy(img1);
				// get text from textbox in encode menu item
				String text = textbox.getText();
				// encode user's message into the selected image
				BufferedImage imageWithMessage = calc.encodeMessage(text, img);
				// so now we have a new image with the encoded message
				// want to save it in the current directory with a new name
				saveImage(imageWithMessage);
			}

		});

		decodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				//use filetodecode
				//first check that it isn't null
				//if it is, output dialog
				if (filetodecode == null)
					JOptionPane.showMessageDialog(null, "Select 'decode' from file menu to choose image first");
				else {
					String x = calc.decodeMessage(filetodecode);
					encodeMenu();
					textbox.setText(x);}
				
			}
		});

		// upon clicking 'encode' in menu

		encode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				encodeMenu();
			}
		});

		// upon clicking 'decode' in menu
		decode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				decodeMenu();
			}
		});
		// upon clicking 'exit' in menu
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});

		encodeMenu();

		file.add(encode);
		file.addSeparator();
		file.add(decode);
		file.addSeparator();
		file.add(exit);

		menu.add(file);
		setJMenuBar(menu);

		// attributes of JFrame window
		setResizable(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(550, 550);
		setBackground(Color.lightGray);
		setVisible(true);

	}

	// to save image
	public void saveImage(BufferedImage newimage) {

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");

		int userSelection = fileChooser.showSaveDialog(this);
		File fileToSave = null;
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			fileToSave = fileChooser.getSelectedFile();
			// System.out.println("Save as file: " + fileToSave.getAbsolutePath());
		}

		try {
			ImageIO.write(newimage, "png", fileToSave);
			JOptionPane.showMessageDialog(null, "Your new image has been successfully saved!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error in saving!");
			e.printStackTrace();
		}

	}

	/*
	 * Every time the encode item is chosen from menu, reset it to empty panel/text
	 */
	public void encodeMenu() {

		textbox.setText("Enter your secret message here");

		// setLayout(new BorderLayout());
		setContentPane(encodepanel);
		validate();
		setVisible(true);
	}

	public void decodeMenu() {
		setContentPane(decodepanel);
		File x = selectFile();
		filetodecode = x;
		JLabel picLabel = new JLabel(new ImageIcon("" + x));
		add(picLabel);
		validate();
		setVisible(true);
	}

	public File selectFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(this);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
		// fileChooser.setAcceptAllFileFilterUsed(false);

		File selectedFile = fileChooser.getSelectedFile();
		/*
		 * File selectedFile = null; if (result ==
		 * JFileChooser.ACCEPT_ALL_FILE_FILTER_USED_CHANGED_PROPERTY()) { selectedFile =
		 * fileChooser.getSelectedFile(); } return selectedFile;
		 */
		return selectedFile;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
