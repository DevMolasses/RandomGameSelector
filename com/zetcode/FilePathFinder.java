package com.zetcode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilePathFinder {

	private JPanel panel;
	public FilePathFinder(File config) {
		panel = new JPanel();
		
		JFileChooser fileopen = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("csv files", "csv");
		fileopen.setFileFilter(filter);
		int ret = fileopen.showDialog(panel, "Open Game List");
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileopen.getSelectedFile();
			String text = file.getPath();
			filePath = text;
			writeNewPathToFile(filePath, config);
		} else {
			System.exit(0);
		}
	}
	
	private void writeNewPathToFile(String filePath, File config) {
		//File newCSVFilePath = new File(config);
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(config));
			writer.write(filePath);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public String getPath() {
		return filePath;
	}
	
	private String filePath;
}
