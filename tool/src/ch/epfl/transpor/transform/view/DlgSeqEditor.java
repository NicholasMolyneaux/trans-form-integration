package ch.epfl.transpor.transform.view;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ch.epfl.transpor.transform.model.AppProperties;
import ch.epfl.transpor.transform.model.ModelType;
import ch.epfl.transpor.transform.model.Sequence;

import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

public class DlgSeqEditor extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private Box parPanel = null;
	
	public String seqName = null;
	public Sequence sequence = null;
	public AppProperties appProps = null;
	
	/**
	 * Create the dialog.
	 */
	public DlgSeqEditor(Sequence seq) {
		setTitle("Sequence Editor");
		setModal(true);
		
		sequence = seq;
		if (seq != null)
			appProps = seq.appProps;
		
		setBounds(100, 100, 479, 280);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		parPanel = Box.createVerticalBox();
		JScrollPane scrollPaneCenter = new JScrollPane(parPanel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		contentPanel.add(scrollPaneCenter, BorderLayout.CENTER);
		
		if(seq == null || seq.steps.size() == 0) {
			addStepPanel(parPanel, ModelType.HUB);
		} else {
			for (int i=0;i<seq.steps.size();i++) {
				addStepPanel(parPanel, seq.steps.get(i));
			}
		}
		
		//upper panel
		JPanel upPanel = new JPanel();
		contentPanel.add(upPanel, BorderLayout.NORTH);
	
		JLabel lbSequenceName = new JLabel("Sequence name:");
		upPanel.add(lbSequenceName);
		
		JTextField tfSeqName = new JTextField();
		upPanel.add(tfSeqName);
		tfSeqName.setMinimumSize(new Dimension(200,30));
		tfSeqName.setPreferredSize(new Dimension(200,30));
		
		JSeparator separator = new JSeparator();
		upPanel.add(separator);
		
		JButton btnSeqCfg = new JButton("Configure");
		btnSeqCfg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				DlgSeqConfigEditor dlg = new DlgSeqConfigEditor(appProps);
				dlg.setVisible(true);
				
				appProps = dlg.appProps;
			}
		});
		upPanel.add(btnSeqCfg);
		if (seq != null) {
			tfSeqName.setText(seq.name);
		}
				
		//right panel
		JPanel rightPanel = new JPanel();
		contentPanel.add(rightPanel, BorderLayout.EAST);
	
		JButton btnAdd = new JButton("Add step");
		
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				addStepPanel(parPanel, ModelType.HUB);
				parPanel.revalidate();
				parPanel.repaint();
				validate();
				
			}
		});
		rightPanel.add(btnAdd);
		
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
			
				sequence = new Sequence();
				sequence.name = tfSeqName.getText();
				sequence.appProps = appProps;
				
				for (int i=0;i<parPanel.getComponents().length;i++) {
					JComboBox cb = (JComboBox) ((JPanel)parPanel.getComponents()[i]).getComponent(0);
					
					if(cb.getSelectedItem().toString().equals("Hub"))
						sequence.steps.add(ModelType.HUB);
					else if(cb.getSelectedItem().toString().equals("Regional"))
						sequence.steps.add(ModelType.REGIONAL);
					else
						sequence.steps.add(ModelType.URBAN);
				}
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
	}
	
	protected void deletePanel(ActionEvent e) {
		JButton btn = (JButton)e.getSource();
		JPanel pnl = (JPanel)btn.getParent();
		for (int i=0;i<parPanel.getComponents().length;i++) {
			if(pnl.equals(parPanel.getComponents()[i]))
			{
				parPanel.remove(i);
				break;
			}
		}
		parPanel.revalidate();
		parPanel.repaint();
		validate();
	}
	
	protected void movePanelDown(ActionEvent e, int step) {
		JButton btn = (JButton)e.getSource();
		JPanel pnl = (JPanel)btn.getParent();
		for (int i=0;i<parPanel.getComponents().length;i++) {
			if(pnl.equals(parPanel.getComponents()[i]) && i<parPanel.getComponents().length-step)
			{
				JComboBox cb1 = (JComboBox) pnl.getComponent(0);
				JComboBox cb2 = (JComboBox) ((JPanel)parPanel.getComponents()[i+step]).getComponent(0);
				
				int tmp = cb1.getSelectedIndex();
				cb1.setSelectedIndex(cb2.getSelectedIndex());
				cb2.setSelectedIndex(tmp);
				
				break;
				
			}
		}
		parPanel.revalidate();
		parPanel.repaint();
		validate();
	}
	
	protected void movePanelUp(ActionEvent e, int step) {
		JButton btn = (JButton)e.getSource();
		JPanel pnl = (JPanel)btn.getParent();
		for (int i=0;i<parPanel.getComponents().length;i++) {
			if(pnl.equals(parPanel.getComponents()[i]) && i>=step)
			{
				JComboBox cb1 = (JComboBox) pnl.getComponent(0);
				JComboBox cb2 = (JComboBox) ((JPanel)parPanel.getComponents()[i-step]).getComponent(0);
				
				int tmp = cb1.getSelectedIndex();
				cb1.setSelectedIndex(cb2.getSelectedIndex());
				cb2.setSelectedIndex(tmp);
				
				break;
			}
		}
		parPanel.revalidate();
		parPanel.repaint();
		validate();
		
	}
	
	protected void addStepPanel(Box parPanel, ModelType mt)
	{
		JPanel panel = new JPanel();
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Hub", "Regional", "Urban"}));
		switch(mt) {
		case HUB:
			comboBox.setSelectedIndex(0);
			break;
		case REGIONAL:
			comboBox.setSelectedIndex(1);
			break;
		default:
			comboBox.setSelectedIndex(2);
		}
		panel.add(comboBox);
		
		JButton btnDelete = new JButton("Delete");
		panel.add(btnDelete);
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deletePanel(e);
			}
		});
		
		/*JButton btnUp = new JButton("Up");
		panel.add(btnUp);
		btnUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movePanelUp(e,1);
			}
		});
		JButton btnDown = new JButton("Down");
		panel.add(btnDown);
		btnDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				movePanelDown(e,1);
			}
		});*/
		
		parPanel.add(panel);
	}

}
