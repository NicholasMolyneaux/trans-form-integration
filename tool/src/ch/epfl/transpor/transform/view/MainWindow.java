package ch.epfl.transpor.transform.view;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.awt.Cursor;

import javax.swing.UIManager;

import ch.epfl.transpor.transform.controller.ModelRunner;
import ch.epfl.transpor.transform.controller.SequenceHandler;
import ch.epfl.transpor.transform.model.AppProperties;
import ch.epfl.transpor.transform.model.ModelType;
import ch.epfl.transpor.transform.model.Sequence;
import ch.epfl.transpor.transform.model.exceptions.ModelFailedException;
import ch.epfl.transpor.transform.model.exceptions.RunModelException;

import net.miginfocom.swing.MigLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.border.TitledBorder;

import javax.swing.JSpinner;

public class MainWindow {

	private JFrame frmTransformIntegrationTool;
	private JTextField txtGraph;
	private JTextField txtBMInput;
	private JTextField txtRegInput;
	private JFileChooser fc = new JFileChooser();
	private JTextField txtRegOutput;
	private JSpinner spnSimTime;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmTransformIntegrationTool.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTransformIntegrationTool = new JFrame();
		frmTransformIntegrationTool.setTitle("TRANS-FORM Integration Tool");
		frmTransformIntegrationTool.setBounds(100, 100, 662, 580);
		frmTransformIntegrationTool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTransformIntegrationTool.getContentPane().setLayout(new BoxLayout(frmTransformIntegrationTool.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Hub model", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frmTransformIntegrationTool.getContentPane().add(panel);
		panel.setLayout(new MigLayout("", "[120px][grow][70px]", "[][]"));
		
		JLabel lblGraphData = new JLabel("Config file:");
		panel.add(lblGraphData, "cell 0 0,alignx trailing");
		
		txtGraph = new JTextField();
		panel.add(txtGraph, "cell 1 0,growx");
		txtGraph.setColumns(10);
		txtGraph.setText(AppProperties.getDefaultProps().getHubCfgFile());
		
		JButton btnGraph = new JButton("...");
		btnGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtGraph.setText(file.getPath());
		        }
			}
		});
		panel.add(btnGraph, "cell 2 0");
		
		JButton btnRunHub = new JButton("Run");
		panel.add(btnRunHub, "cell 2 1");
		btnRunHub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runModel(new Sequence(ModelType.HUB));
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Urban model", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		frmTransformIntegrationTool.getContentPane().add(panel_1);
		panel_1.setLayout(new MigLayout("", "[120px][grow][70px]", "[][]"));
		
		JLabel lblInputFile = new JLabel("Master file:");
		panel_1.add(lblInputFile, "cell 0 0,alignx trailing");
		
		txtBMInput = new JTextField();
		panel_1.add(txtBMInput, "cell 1 0,growx");
		txtBMInput.setColumns(10);
		txtBMInput.setText(AppProperties.getDefaultProps().getBMMasterFile());
		
		JButton btnBMInput = new JButton("...");
		btnBMInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtBMInput.setText(file.getPath());
		        }
			}
		});
		panel_1.add(btnBMInput, "cell 2 0");
		
		JButton btnRunUrban = new JButton("Run");
		panel_1.add(btnRunUrban, "cell 2 1");
		btnRunUrban.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runModel(new Sequence(ModelType.URBAN));
			}
		});
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Regional model", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		frmTransformIntegrationTool.getContentPane().add(panel_2);
		panel_2.setLayout(new MigLayout("", "[120px][grow][70px]", "[][][]"));
		
		JLabel lblInputFile_1 = new JLabel("Input folder:");
		panel_2.add(lblInputFile_1, "cell 0 0,alignx trailing");
		
		txtRegInput = new JTextField();
		panel_2.add(txtRegInput, "cell 1 0,growx");
		txtRegInput.setColumns(10);
		txtRegInput.setText(AppProperties.getDefaultProps().getRegInput());
		
		JButton btnRegInput = new JButton("...");
		btnRegInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtRegInput.setText(file.getPath());
		        }
			}
		});
		panel_2.add(btnRegInput, "cell 2 0");
		
		JLabel lblOutputFolder = new JLabel("Output folder:");
		panel_2.add(lblOutputFolder, "cell 0 1,alignx trailing");
		
		txtRegOutput = new JTextField();
		panel_2.add(txtRegOutput, "cell 1 1,growx");
		txtRegOutput.setColumns(10);
		txtRegOutput.setText(AppProperties.getDefaultProps().getRegOutput());
		
		JButton btnRegOutput = new JButton("...");
		btnRegOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtRegOutput.setText(file.getPath());
		        }
			}
		});
		panel_2.add(btnRegOutput, "cell 2 1");
		
		JButton btnRunReg = new JButton("Run");
		panel_2.add(btnRunReg, "cell 2 2");
		btnRunReg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runModel(new Sequence(ModelType.REGIONAL));
			}
		});
			
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Sequence of models", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frmTransformIntegrationTool.getContentPane().add(panel_3);
		panel_3.setLayout(new MigLayout("", "[120px][grow][70px]", "[][]"));
		
		JLabel lblChooseSequence = new JLabel("Choose sequence:");
		panel_3.add(lblChooseSequence, "cell 0 0");
		
		JComboBox cbSequence = new JComboBox();
		panel_3.add(cbSequence, "cell 1 0");
		
		ArrayList<String> seqNames = SequenceHandler.getInstance().getSequenceNames();
		for(int i=0;i<seqNames.size();i++)
			cbSequence.addItem(seqNames.get(i));
		
		JButton btnRunSequence = new JButton("Run");
		panel_3.add(btnRunSequence, "cell 2 0");
		
		JLabel lblNewLabel = new JLabel("Simulation start time:");
		panel_3.add(lblNewLabel, "cell 0 1");
		
		spnSimTime = new JSpinner(new SpinnerDateModel());
		Date dt = new Date();
		dt.setHours(6);
		dt.setMinutes(0);
		dt.setSeconds(0);
		((SpinnerDateModel)spnSimTime.getModel()).setValue(dt);
		panel_3.add(spnSimTime, "cell 1 1");
		
		btnRunSequence.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Sequence seq = SequenceHandler.getInstance().getSequence(cbSequence.getSelectedItem().toString());
				if(seq != null)
					runModel(seq);
				
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		frmTransformIntegrationTool.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JDialog dlgSettings = new DlgSettings();
				dlgSettings.setVisible(true);
				
			}
		});
		mnFile.add(mntmSettings);
		
		JMenuItem mntmEditSequences = new JMenuItem("Edit Sequences");
		mntmEditSequences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JDialog dlgSequences = new DlgSequences();
				dlgSequences.setVisible(true);
				
				//refill cb
				cbSequence.removeAllItems();
				ArrayList<String> seqNames = SequenceHandler.getInstance().getSequenceNames();
				for(int i=0;i<seqNames.size();i++)
					cbSequence.addItem(seqNames.get(i));
				
			}
		});
		mnFile.add(mntmEditSequences);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmTransformIntegrationTool.dispose();
			}
		});
		
		JMenu mnHelp = new JMenu("About");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDlgAbout dlgAbout = new JDlgAbout();
				dlgAbout.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);
		
	}

	protected void runModel(Sequence seq) {
		
		frmTransformIntegrationTool.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		AppProperties.getDefaultProps().setHubCfgFile(txtGraph.getText());
		AppProperties.getDefaultProps().setBMMasterFile(txtBMInput.getText());
		AppProperties.getDefaultProps().setRegInputOutput(txtRegInput.getText(), txtRegOutput.getText());
		AppProperties.getDefaultProps().storeDeafultAppProps();
		
		ModelRunner mr = new ModelRunner();
		try {
			mr.runSequence(seq, (Date)spnSimTime.getValue());
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver(s) successfully executed.",
				    "Info", JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver of the input file does not exist! Details:\n" + ex.getMessage(),
				    "Error", JOptionPane.ERROR_MESSAGE);
		} catch (RunModelException e) {
			
			switch(e.modelType) {
			case URBAN:
				JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Not all input files are available. Please run the urban model first.",
					    "Error", JOptionPane.ERROR_MESSAGE);
				break;
			case HUB:
				JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Not all input files are available. Please run the hub model first.",
					    "Error", JOptionPane.ERROR_MESSAGE);
				break;
			case REGIONAL:
				JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Not all input files are available. Please run the regional model first.",
					    "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver failed to execute (InterruptedException)! Details:\n" + e.getMessage(),
				    "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver of the input file does not exist! Details:\n" + e.getMessage(),
				    "Error", JOptionPane.ERROR_MESSAGE);
		} catch (ModelFailedException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver failed to execute (ModelFailedException)! Details:\n" + e.getMessage(),
				    "Error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frmTransformIntegrationTool, "Solver failed to execute (Exception)! Details:\n" + e.getMessage(),
				    "Error", JOptionPane.ERROR_MESSAGE);
		}
		finally {
			frmTransformIntegrationTool.setCursor(Cursor.getDefaultCursor());
		}
		
	}

}
