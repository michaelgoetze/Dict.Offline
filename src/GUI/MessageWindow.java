package GUI;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class MessageWindow {

	public static void showMessageDialog(String text, String name, int image) {
		JOptionPane.showMessageDialog(null, text, name, image);
		System.out.println(getDate() + "Message: " + name + " - " + text);
	}

	static void showScrollableMessageDialog(String text, String name, int image) {
		JLabel textArea = new JLabel(text);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(800, 500));

		JOptionPane.showMessageDialog(null, scrollPane, name, image);
		System.out.println(getDate() + "Message: " + name + " - " + text);
	}

	public static void showErrorDialog(String text, Exception e) {

		e.printStackTrace();
		System.out.println(getDate() + "Error: - " + text + "\n " + e.toString());
		text = "<html><body style='width:200px'>" + text + "<br> " + e;
		JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static String getDate() {
		return new SimpleDateFormat().format(Calendar.getInstance().getTime()) + " - ";
	}

}
