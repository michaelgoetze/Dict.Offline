package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SplashScreen extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SplashScreen() {
		super();
		setUndecorated(true);
		setBackground(new Color(1.0f, 1.0f, 1.0f, 0f));
		setSize(512, 512);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
		this.setUndecorated(true);
		JLabel picture = new JLabel();
		ImageIcon gifImage = new ImageIcon(getClass().getResource("DictOffline.gif"));
		picture = new JLabel((gifImage));
		picture.setOpaque(false);
		getContentPane().add(picture);
		setVisible(true);
	}
}
