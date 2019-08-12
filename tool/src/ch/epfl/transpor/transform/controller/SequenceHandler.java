package ch.epfl.transpor.transform.controller;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.epfl.transpor.transform.model.Sequence;

public class SequenceHandler {
	
	protected String seqsFile = "sequences.dat";
	
	protected HashMap<String,Sequence> sequences = new HashMap<String,Sequence>();
	
	private static SequenceHandler instance = null;
	
	private SequenceHandler() {
		LoadSequences();
	}
	
	public static SequenceHandler getInstance() {
		if(instance == null)
			instance = new SequenceHandler();
		
		return instance;
	}
	
	public void SaveSequences() {
		
		try {
			FileOutputStream fout = new FileOutputStream(seqsFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(sequences);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Sequence getSequence(String name){
		
		return sequences.get(name);		
	}
	
	public void updateSequence(Sequence seq) {
		sequences.put(seq.name, seq);
	}
	
	public void removeSequence(String seqName) {
		sequences.remove(seqName);
	}
	
	@SuppressWarnings("unchecked")
	private void LoadSequences(){
		File sf = new File(seqsFile);
		try {
			if(!sf.exists()) {
				sf.createNewFile();
				sequences = new HashMap<String,Sequence>();
			} else {
				
				FileInputStream fin = new FileInputStream(seqsFile);
				ObjectInputStream ois = new ObjectInputStream(fin);
				Object obj = ois.readObject();
				if (obj instanceof HashMap<?,?>)
					sequences = (HashMap<String,Sequence>)obj; 
				ois.close();
			}
		} catch (EOFException e) {
			sequences = new HashMap<String,Sequence>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getSequenceNames(){
		ArrayList<String> ret = new ArrayList<String>();
		
		Iterator<String> it = sequences.keySet().iterator();
		while(it.hasNext())
			ret.add(it.next());
		
		return ret;
	}

}
