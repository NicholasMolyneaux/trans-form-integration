package ch.epfl.transpor.transform.view;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ch.epfl.transpor.transform.controller.SequenceHandler;

import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class DlgSequences extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private DefaultListModel<String> dlm = new DefaultListModel<String>();
	private JList<String> list = new JList<String>(dlm);

	/**
	 * Create the dialog.
	 */
	public DlgSequences() {
		setTitle("Sequences");
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			contentPanel.add(list);
			updateSeqList();
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.EAST);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			{
				JButton btnAdd = new JButton("Add");
				btnAdd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						DlgSeqEditor dlgSeqEdit = new DlgSeqEditor(null);
						dlgSeqEdit.setVisible(true);
						if(dlgSeqEdit.sequence != null) {
							SequenceHandler.getInstance().updateSequence(dlgSeqEdit.sequence);
							updateSeqList();
						}
					}
				});
				panel.add(btnAdd);
			}
			{
				JButton btnEdit = new JButton("Edit");
				btnEdit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String seqName = list.getSelectedValue().toString();
						DlgSeqEditor dlgSeqEdit = new DlgSeqEditor(SequenceHandler.getInstance().getSequence(seqName));
						dlgSeqEdit.setVisible(true);
						if(dlgSeqEdit.sequence != null) {
							if(!seqName.equals(dlgSeqEdit.sequence.name))
								SequenceHandler.getInstance().removeSequence(seqName);
							SequenceHandler.getInstance().updateSequence(dlgSeqEdit.sequence);
							updateSeqList();
						}
					}
				});
				panel.add(btnEdit);
			}
			{
				JButton btnDelete = new JButton("Delete");
				btnDelete.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String seqName = list.getSelectedValue().toString();
						SequenceHandler.getInstance().removeSequence(seqName);
						updateSeqList();
					}
				});
				panel.add(btnDelete);
			}
		}
		{
			JLabel lblNewLabel = new JLabel("Saved sequences:");
			contentPanel.add(lblNewLabel, BorderLayout.NORTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SequenceHandler.getInstance().SaveSequences();
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

	private void updateSeqList() {
		
		dlm.removeAllElements();
		
		List<String> names = SequenceHandler.getInstance().getSequenceNames();
		for(int i=0;i<names.size();i++) {
			dlm.addElement(names.get(i));
		}
		
	}

}
