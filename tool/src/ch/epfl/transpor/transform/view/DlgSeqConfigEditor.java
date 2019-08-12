package ch.epfl.transpor.transform.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import ch.epfl.transpor.transform.model.AppProperties;

import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.Box;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.io.File;


public class DlgSeqConfigEditor extends JDialog {

	private static final long serialVersionUID = -7795330288305033823L;
	private JTextField txtHubConfig;
	private JTextField txtBMInput;
	private JTextField txtRegInput;
	private JFileChooser fc = new JFileChooser();
	private JTextField txtRegOutput;
	private JTextField txtHubSolver;
	private JTextField txtUrbanSolver;
	private JTextField txtRegSolver;

	public AppProperties appProps = null;
	

	/**
	 * Create the application.
	 * @param appProps 
	 */
	public DlgSeqConfigEditor(AppProperties ap) {
		setTitle("Sequence Configuration");
		initialize(ap);
	}

	/**
	 * Initialize the contents of the frame.
	 * @param appProps 
	 */
	private void initialize(AppProperties ap) {
		
		setModal(true);	
		this.appProps = ap;
		
		setBounds(100, 100, 562, 551);
		getContentPane().setLayout(new BorderLayout());
		
		Box manPan = Box.createVerticalBox();
		getContentPane().add(manPan, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		manPan.add(panel);
		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Hub model", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(new MigLayout("", "[90px][grow][70px]", "[][]"));
		
		JLabel lblNewLabel = new JLabel("Solver:");
		panel.add(lblNewLabel, "cell 0 0,alignx trailing");
		
		txtHubSolver = new JTextField();
		panel.add(txtHubSolver, "cell 1 0,growx");
		txtHubSolver.setColumns(10);
		if (appProps != null)
			txtHubSolver.setText(appProps.getHubSolver());
		else
			txtHubSolver.setText(AppProperties.getDefaultProps().getHubSolver());
		
		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtHubSolver.setText(file.getPath());
		        }
			}
		});
		panel.add(button, "cell 2 0");
		
		JLabel lblGraphData = new JLabel("Config file:");
		panel.add(lblGraphData, "cell 0 1,alignx trailing");
		
		txtHubConfig = new JTextField();
		panel.add(txtHubConfig, "cell 1 1,growx");
		txtHubConfig.setColumns(10);
		if (appProps != null)
			txtHubConfig.setText(appProps.getHubCfgFile());
		else
			txtHubConfig.setText(AppProperties.getDefaultProps().getHubCfgFile());
		
		JButton btnGraph = new JButton("...");
		btnGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtHubConfig.setText(file.getPath());
		        }
			}
		});
		panel.add(btnGraph, "cell 2 1");
		
		JPanel panel_1 = new JPanel();
		manPan.add(panel_1);
		panel_1.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Urban model", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setLayout(new MigLayout("", "[90px][grow][70px]", "[][]"));
		
		JLabel lblNewLabel_1 = new JLabel("Solver:");
		panel_1.add(lblNewLabel_1, "cell 0 0,alignx trailing");
		
		txtUrbanSolver = new JTextField();
		panel_1.add(txtUrbanSolver, "cell 1 0,growx");
		txtUrbanSolver.setColumns(10);
		if (appProps != null)
			txtUrbanSolver.setText(appProps.getUrbanSolver());
		else
			txtUrbanSolver.setText(AppProperties.getDefaultProps().getUrbanSolver());
		
		JButton button_2 = new JButton("...");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtUrbanSolver.setText(file.getPath());
		        }
			}
		});
		panel_1.add(button_2, "cell 2 0");
		
		JLabel lblInputFile = new JLabel("Master file:");
		panel_1.add(lblInputFile, "cell 0 1,alignx trailing");
		
		txtBMInput = new JTextField();
		panel_1.add(txtBMInput, "cell 1 1,growx");
		txtBMInput.setColumns(10);
		if (appProps != null)
			txtBMInput.setText(appProps.getBMMasterFile());
		else
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
		panel_1.add(btnBMInput, "cell 2 1");
		
		JPanel panel_2 = new JPanel();
		manPan.add(panel_2);
		panel_2.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Regional model", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_2.setLayout(new MigLayout("", "[90px][grow][70px]", "[][][]"));
		
		JLabel lblSolver = new JLabel("Solver:");
		panel_2.add(lblSolver, "cell 0 0,alignx trailing");
		
		txtRegSolver = new JTextField();
		panel_2.add(txtRegSolver, "cell 1 0,growx");
		txtRegSolver.setColumns(10);
		if (appProps != null)
			txtRegSolver.setText(appProps.getRegionalSolver());
		else
			txtRegSolver.setText(AppProperties.getDefaultProps().getRegionalSolver());
		
		JButton button_1 = new JButton("...");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fc.showOpenDialog(panel.getParent());
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            txtRegSolver.setText(file.getPath());
		        }
			}
		});
		panel_2.add(button_1, "cell 2 0");
		
		JLabel lblInputFile_1 = new JLabel("Input folder:");
		panel_2.add(lblInputFile_1, "cell 0 1,alignx trailing");
		
		txtRegInput = new JTextField();
		panel_2.add(txtRegInput, "cell 1 1,growx");
		txtRegInput.setColumns(10);
		if (appProps != null)
			txtRegInput.setText(appProps.getRegInput());
		else
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
		panel_2.add(btnRegInput, "cell 2 1");
		
		JLabel lblOutputFolder = new JLabel("Output folder:");
		panel_2.add(lblOutputFolder, "cell 0 2,alignx trailing");
		
		txtRegOutput = new JTextField();
		panel_2.add(txtRegOutput, "cell 1 2,growx");
		txtRegOutput.setColumns(10);
		if (appProps != null)
			txtRegOutput.setText(appProps.getRegOutput());
		else
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
		panel_2.add(btnRegOutput, "cell 2 2");
		
		//button panel
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				appProps = new AppProperties();
				appProps.setHubSolver(txtHubSolver.getText());
				appProps.setHubCfgFile(txtHubConfig.getText());
				appProps.setUrbanSolver(txtUrbanSolver.getText());
				appProps.setBMMasterFile(txtBMInput.getText());
				appProps.setRegionalSolver(txtRegSolver.getText());
				appProps.setRegInputOutput(txtRegInput.getText(),txtRegOutput.getText());
				
				dispose();
				
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});	
		
		validate();
		repaint();
		
	}

}
