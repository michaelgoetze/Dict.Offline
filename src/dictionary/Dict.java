package dictionary;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import GUI.MainFrame;
import GUI.MessageWindow;
import GUI.SplashScreen;

/*
 * Functions to add:
 * - autofill
 */

public class Dict {

	public static Dictionary dict;
	public static Image icon;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("DictOffline.png"));

				String dictFile = getProperty("DICT_DIR");
				if (dictFile.equals("") || !(new File(dictFile).exists())) {
					MessageWindow.showMessageDialog(
							"Kein Wörterbuch gefunden! Laden sie bitte ein passendes Wörterbuch (Dict.cc) herunter und wählen Sie es im nächsetn Schritt aus",
							"Wörterbuch-datei auswählen", JOptionPane.INFORMATION_MESSAGE);
					Frame f = new Frame("dummy");
					f.setIconImage(icon);
					JFileChooser fc = new JFileChooser(getHomeDir());

					int returnVal = fc.showOpenDialog(f);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						dictFile = fc.getSelectedFile().getAbsolutePath();
						if (new File(dictFile).exists()) {
							updateIni("DICT_DIR", dictFile);
						}
					}
				}

				if (new File(dictFile).exists()) {
					loadDictionary(dictFile);
				} else {
					MessageWindow.showMessageDialog("Could not load Dictionary, ending software", "Error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});

	}

	public static ArrayList<Entry> getExactEntries(String word) {
		return dict.getExactEntries(word);// dict.getExactEntries(word);
	}

	public static ArrayList<Entry> getPartialMatchedEntries(String word) {
		return dict.getPartialMatchedEntries(word);// dict.getExactEntries(word);
	}

	public static void loadDictionary(String dictFile) {
		final SplashScreen splash = new SplashScreen();
		splash.setIconImage(icon);

		SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {

			@Override
			protected void done() {
				// Close the dialog
				splash.dispose();
				MainFrame frame = new MainFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}

			@Override
			protected Object doInBackground() throws Exception {
				dict = new Dictionary(dictFile);

				return null;
			}

		};
		worker.execute();
	}

	public static String getPropertyOrDefault(String property, String defaultString) {
		String value = getProperty(property);
		if (value.equals(""))
			return defaultString;
		return value;
	}

	public static String getProperty(String property) {
		String value = "";
		try {
			BufferedReader iniFile;
			iniFile = new BufferedReader(new FileReader(new File("Dict.ini")));
			String line = iniFile.readLine();
			while (line != null) {
				if (line.startsWith(property)) {
					String[] dataArray = line.split("=");
					value = dataArray[1];
				}
				line = iniFile.readLine();
			}
			iniFile.close();
		} catch (Exception e) {
		}

		return value;
	}

	public static String getHomeDir() {
		return Paths.get("").toAbsolutePath().toString() + File.separator;
	}

	public static void updateIni(String property, String value) {
		updateIni(property, value, 0);
	}

	public static void updateIni(String property, String value, int attempt) {
		String s = getHomeDir() + "Dict.ini";
		final List<String> lines = new LinkedList<String>();
		BufferedReader iniFile;
		File f = new File(s);

		boolean foundProperty = false;
		try {
			if (f.exists() == false) {
				f.createNewFile();
			} else {
				iniFile = new BufferedReader(new FileReader(new File(s)));

				String line = iniFile.readLine();
				while (line != null) {
					if (line.contains(property)) {
						lines.add(property + "=" + value);
						foundProperty = true;
					} else {
						lines.add(line);
					}
					line = iniFile.readLine();
				}
				iniFile.close();
			}
			if (foundProperty == false) {
				lines.add(property + "=" + value);
			}

			PrintWriter pw = null;
			Writer fw = new FileWriter(s);
			Writer bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			for (final String printLine : lines) {
				pw.println(printLine);
			}

			if (pw != null) {
				pw.close();
			}
		} catch (IOException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			if (attempt < 5) {
				updateIni(property, value, attempt++);
			} else {
				MessageWindow.showErrorDialog("Error writing Dict.ini file", e);
			}
		}
	}
}
