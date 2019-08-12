package ch.epfl.transpor.transform.view;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ch.epfl.transpor.transform.model.AppProperties;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;

public class DlgSettings extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField tfHubSolver;
	private JTextField tfUrbanSolver;
	private JTextField tfRegSolver;
	private JFileChooser fc = new JFileChooser();

	/**
	 * Create the dialog.
	 */
	public DlgSettings() {
		setTitle("Settings");
		setBounds(100, 100, 548, 212);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new MigLayout("", "[130px,right][300px,grow,left][40px]", "[][][]"));
		{
			JLabel lblHubSolver = new JLabel("Hub solver:");
			contentPanel.add(lblHubSolver, "cell 0 0,alignx trailing");
		}
		{
			tfHubSolver = new JTextField();
			contentPanel.add(tfHubSolver, "cell 1 0,growx");
			tfHubSolver.setColumns(10);
			tfHubSolver.setText(AppProperties.getDefaultProps().getHubSolver());
		}
		{
			JButton btnHubSolver = new JButton("...");
			btnHubSolver.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == btnHubSolver) {
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int returnVal = fc.showOpenDialog(contentPanel.getParent());
				        if (returnVal == JFileChooser.APPROVE_OPTION) {
				            File file = fc.getSelectedFile();
				            tfHubSolver.setText(file.getPath());
				        } 
					}
				}
			});
			contentPanel.add(btnHubSolver, "cell 2 0");
		}
		{
			JLabel lblUrbanSolver = new JLabel("Urban solver:");
			contentPanel.add(lblUrbanSolver, "cell 0 1,alignx trailing");
		}
		{
			tfUrbanSolver = new JTextField();
			contentPanel.add(tfUrbanSolver, "cell 1 1,growx");
			tfUrbanSolver.setColumns(10);
			tfUrbanSolver.setText(AppProperties.getDefaultProps().getUrbanSolver());
		}
		{
			JButton btnUrbanSolver = new JButton("...");
			btnUrbanSolver.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == btnUrbanSolver) {
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int returnVal = fc.showOpenDialog(contentPanel.getParent());
				        if (returnVal == JFileChooser.APPROVE_OPTION) {
				            File file = fc.getSelectedFile();
				            tfUrbanSolver.setText(file.getPath());
				        } 
					}
				}
			});
			contentPanel.add(btnUrbanSolver, "cell 2 1");
		}
		{
			JLabel lblRegionalSolver = new JLabel("Regional solver:");
			contentPanel.add(lblRegionalSolver, "cell 0 2,alignx trailing");
		}
		{
			tfRegSolver = new JTextField();
			contentPanel.add(tfRegSolver, "cell 1 2,growx");
			tfRegSolver.setColumns(10);
			tfRegSolver.setText(AppProperties.getDefaultProps().getRegionalSolver());
		}
		{
			JButton btnRegSolver = new JButton("...");
			btnRegSolver.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getSource() == btnRegSolver) {
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int returnVal = fc.showOpenDialog(contentPanel.getParent());
				        if (returnVal == JFileChooser.APPROVE_OPTION) {
				            File file = fc.getSelectedFile();
				            tfRegSolver.setText(file.getPath());
				        } 
					}
				}
			});
			contentPanel.add(btnRegSolver, "cell 2 2");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AppProperties.getDefaultProps().setUrbanSolver(tfUrbanSolver.getText());
						AppProperties.getDefaultProps().setHubSolver(tfHubSolver.getText());
						AppProperties.getDefaultProps().setRegionalSolver(tfRegSolver.getText());
						AppProperties.getDefaultProps().storeDeafultAppProps();
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
