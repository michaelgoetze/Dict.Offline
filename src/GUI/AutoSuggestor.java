package GUI;

/**
 * modified after David Kroukamp and syb0rg from StackOverFlow.com
 * 
 * https://stackoverflow.com/questions/14186955/create-a-autocompleting-textbox-in-java-with-a-dropdown-list
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AutoSuggestor {

	private final JTextField textField;
	private final Window container;
	private JPanel suggestionsPanel;
	private JWindow autoSuggestionPopUpWindow;
	private String typedWord;
	private final ArrayList<String> dictionary = new ArrayList<>();
	private int currentIndexOfSpace, tW, tH;
	private Font f;
	private boolean active = true;
	private ExecutorService executor;
	private List<SearchWords> threadList;

	private DocumentListener documentListener = new DocumentListener() {
		@Override
		public void insertUpdate(DocumentEvent de) {
			checkForAndShowSuggestions();
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			checkForAndShowSuggestions();
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
			checkForAndShowSuggestions();
		}
	};
	private final Color suggestionsTextColor;
	private final Color suggestionFocusedColor;

	public AutoSuggestor(JTextField textField, Window mainWindow, ArrayList<String> words, Color popUpBackground,
			Color textColor, Color suggestionFocusedColor, float opacity, float fontSize) {
		this.textField = textField;
		this.suggestionsTextColor = textColor;
		this.container = mainWindow;
		this.suggestionFocusedColor = suggestionFocusedColor;
		this.textField.getDocument().addDocumentListener(documentListener);
		this.executor = new ThreadPoolExecutor(1,
				// thread pool size
				1, // maximum queue
				1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1, true),
				new ThreadPoolExecutor.CallerRunsPolicy());
		this.threadList = Collections.synchronizedList(new ArrayList<SearchWords>());
		f = new JLabel().getFont().deriveFont(fontSize);
		setDictionary(words);

		typedWord = "";
		currentIndexOfSpace = 0;
		tW = 0;
		tH = 0;

		autoSuggestionPopUpWindow = new JWindow(mainWindow);
		autoSuggestionPopUpWindow.setOpacity(opacity);

		suggestionsPanel = new JPanel();
		suggestionsPanel.setLayout(new GridLayout(0, 1));
		suggestionsPanel.setBackground(popUpBackground);
		suggestionsPanel.setBorder(BorderFactory.createBevelBorder(1));

		addKeyBindingToRequestFocusInPopUpWindow();
	}

	public void setFont(Font f) {
		this.f = f;
	}

	@SuppressWarnings("serial")
	private void addKeyBindingToRequestFocusInPopUpWindow() {
		textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
				"Down released");
		textField.getActionMap().put("Down released", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {// focuses the first label on popwindow
				if (!autoSuggestionPopUpWindow.isVisible()) {
					setFocusToTextField();
					checkForAndShowSuggestions();
				}
				for (int i = 0; i < suggestionsPanel.getComponentCount(); i++) {
					if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
						((SuggestionLabel) suggestionsPanel.getComponent(i)).setFocused(true);
						autoSuggestionPopUpWindow.toFront();
						autoSuggestionPopUpWindow.requestFocusInWindow();
						suggestionsPanel.requestFocusInWindow();
						suggestionsPanel.getComponent(i).requestFocusInWindow();

						break;
					}
				}

			}
		});
		suggestionsPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "Down released");
		suggestionsPanel.getActionMap().put("Down released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent ae) {// allows scrolling of labels in pop window (I know very hacky
															// for now :))

				ArrayList<SuggestionLabel> sls = getAddedSuggestionLabels();
				int max = sls.size();
				boolean foundFocused = false;
				if (max > 1) {// more than 1 suggestion
					for (int i = 0; i < max; i++) {
						SuggestionLabel sl = sls.get(i);
						if (sl.isFocused()) {
							if (i != max - 1) {
								sl.setFocused(false);
								foundFocused = true;
							}
						} else if (i < max) {
							if (foundFocused) {
								sl.setFocused(true);
								autoSuggestionPopUpWindow.toFront();
								autoSuggestionPopUpWindow.requestFocusInWindow();
								suggestionsPanel.requestFocusInWindow();
								suggestionsPanel.getComponent(i).requestFocusInWindow();
								break;
							}
						}
					}
				} else {// only a single suggestion was given
					autoSuggestionPopUpWindow.setVisible(false);
					setFocusToTextField();
					checkForAndShowSuggestions();// fire method as if document listener change occured and fired it
				}
			}
		});
		suggestionsPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "Up released");
		suggestionsPanel.getActionMap().put("Up released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent ae) {// allows scrolling of labels in pop window (I know very hacky
															// for now :))

				ArrayList<SuggestionLabel> sls = getAddedSuggestionLabels();
				int max = sls.size();
				boolean foundFocused = false;
				if (max > 1) {// more than 1 suggestion
					for (int i = max - 1; i >= 0; i--) {
						SuggestionLabel sl = sls.get(i);
						if (sl.isFocused()) {
							sl.setFocused(false);
							foundFocused = true;
							if (i == 0) {
								autoSuggestionPopUpWindow.setVisible(false);
								setFocusToTextField();
							}
						} else if (i < max) {
							if (foundFocused) {
								sl.setFocused(true);
								autoSuggestionPopUpWindow.toFront();
								autoSuggestionPopUpWindow.requestFocusInWindow();
								suggestionsPanel.requestFocusInWindow();
								suggestionsPanel.getComponent(i).requestFocusInWindow();
								break;
							}
						}
					}
				} else {// only a single suggestion was given
					autoSuggestionPopUpWindow.setVisible(false);
					setFocusToTextField();
					checkForAndShowSuggestions();// fire method as if document listener change occured and fired it
				}
			}
		});
		suggestionsPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "Escape released");
		suggestionsPanel.getActionMap().put("Escape released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent ae) {// allows scrolling of labels in pop window (I know very hacky
															// for now :))
				autoSuggestionPopUpWindow.setVisible(false);
				setFocusToTextField();
			}
		});
		textField.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), "Escape released");
		textField.getActionMap().put("Escape released", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent ae) {// allows scrolling of labels in pop window (I know very hacky
															// for now :))
				autoSuggestionPopUpWindow.setVisible(false);
				setFocusToTextField();
			}
		});
	}

	private void setFocusToTextField() {
		container.toFront();
		container.requestFocusInWindow();
		textField.requestFocusInWindow();
	}

	public ArrayList<SuggestionLabel> getAddedSuggestionLabels() {
		ArrayList<SuggestionLabel> sls = new ArrayList<>();
		for (int i = 0; i < suggestionsPanel.getComponentCount(); i++) {
			if (suggestionsPanel.getComponent(i) instanceof SuggestionLabel) {
				SuggestionLabel sl = (SuggestionLabel) suggestionsPanel.getComponent(i);
				sls.add(sl);
			}
		}
		return sls;
	}

	private void checkForAndShowSuggestions() {
		if (isActive()) {
			for (SearchWords thread : threadList) {
				thread.kill();
			}
			typedWord = getCurrentlyTypedWord();

			suggestionsPanel.removeAll();// remove previos words/jlabels that were added

			// used to calcualte size of JWindow as new Jlabels are added
			tW = 0;
			tH = 0;

			SearchWords worker = new SearchWords(typedWord);
			executor.submit(worker);
			this.threadList.add(worker);

		}
	}

	protected void addWordToSuggestions(String word) {
		SuggestionLabel suggestionLabel = new SuggestionLabel(word, suggestionFocusedColor, suggestionsTextColor, this,
				f);

		calculatePopUpWindowSize(suggestionLabel);

		suggestionsPanel.add(suggestionLabel);
	}

	public String getCurrentlyTypedWord() {// get newest word after last white spaceif any or the first word if no white
											// spaces
		String text = textField.getText();
		String wordBeingTyped = "";
		if (text.contains(" ")) {
			int tmp = text.lastIndexOf(" ");
			if (tmp >= currentIndexOfSpace) {
				currentIndexOfSpace = tmp;
				wordBeingTyped = text.substring(text.lastIndexOf(" "));
			}
		} else {
			wordBeingTyped = text;
		}
		return wordBeingTyped.trim();
	}

	private void calculatePopUpWindowSize(JLabel label) {
		// so we can size the JWindow correctly
		if (tW < label.getPreferredSize().width) {
			tW = label.getPreferredSize().width;
		}
		tH += label.getPreferredSize().height;
	}

	private void showPopUpWindow() {
		autoSuggestionPopUpWindow.getContentPane().add(suggestionsPanel);
		autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth(), 30));
		autoSuggestionPopUpWindow.setSize(tW, tH);
		autoSuggestionPopUpWindow.setVisible(true);

		int windowX = 0;
		int windowY = 0;

		windowX = container.getX() + textField.getX() + 10;
		windowY = container.getY() + textField.getY() + 2 * textField.getHeight()
				+ autoSuggestionPopUpWindow.getMinimumSize().height;

		autoSuggestionPopUpWindow.setLocation(windowX, windowY);
		autoSuggestionPopUpWindow.setMinimumSize(new Dimension(textField.getWidth(), 30));
		autoSuggestionPopUpWindow.revalidate();
		autoSuggestionPopUpWindow.repaint();

	}

	public void setDictionary(ArrayList<String> words) {
		dictionary.clear();
		if (words == null) {
			return;// so we can call constructor with null value for dictionary without exception
					// thrown
		}
		for (String word : words) {
			dictionary.add(word);
		}
	}

	public JWindow getAutoSuggestionPopUpWindow() {
		return autoSuggestionPopUpWindow;
	}

	public Window getContainer() {
		return container;
	}

	public JTextField getTextField() {
		return textField;
	}

	public void addToDictionary(String word) {
		dictionary.add(word);
	}

	public boolean wordTyped(String typedWord, SearchWords thread) {
		String[] array = typedWord.split("[\\s-]");
		typedWord = array[array.length - 1];

		boolean suggestionAdded = false;
		ArrayList<String> suggestedWords = new ArrayList<String>();
		if (typedWord.length() > 3) {
			if (typedWord.isEmpty()) {
				return false;
			}
			typedWord = typedWord.toLowerCase();

			for (String word : dictionary) {// get words in the dictionary which we added
				boolean fullymatches = false;
				if (word.length() > typedWord.length()) {
					if (word.toLowerCase().startsWith(typedWord)) {
						fullymatches = true;
					}
				}
				if (fullymatches) {
					suggestedWords.add(word);
					if (suggestedWords.size() > 30)
						return false;
				}
			}
		}
		Collections.sort(suggestedWords, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				Integer l1 = o1.length();
				Integer l2 = o2.length();
				if (l1.equals(l2)) {
					return o1.compareTo(o2);
				}
				return l1.compareTo(l2);
			}

		});
		for (String word : suggestedWords) {
			suggestionAdded = true;
			addWordToSuggestions(word);
		}
		return suggestionAdded;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	private class SearchWords implements Callable<Integer> {
		private String typedWord;
		private boolean isRunning = true;

		private SearchWords(String typedWord) {
			this.typedWord = typedWord;
		}

		@Override
		public Integer call() throws Exception {
			boolean added = wordTyped(typedWord, this);

			if (!added || !isRunning) {
				if (autoSuggestionPopUpWindow.isVisible()) {
					autoSuggestionPopUpWindow.setVisible(false);
				}
			} else {
				showPopUpWindow();
				setFocusToTextField();
			}
			threadList.remove(this);
			return 0;
		}

		public void kill() {
			isRunning = false;
		}
	}
}

@SuppressWarnings("serial")
class SuggestionLabel extends JLabel {

	private boolean focused = false;
	private final JWindow autoSuggestionsPopUpWindow;
	private final JTextField textField;
	private final AutoSuggestor autoSuggestor;
	private Color suggestionsTextColor, suggestionBorderColor;

	public SuggestionLabel(String string, final Color borderColor, Color suggestionsTextColor,
			AutoSuggestor autoSuggestor, Font f) {
		super(string);
		this.setFont(f);
		this.suggestionsTextColor = suggestionsTextColor;
		this.autoSuggestor = autoSuggestor;
		this.textField = autoSuggestor.getTextField();
		this.suggestionBorderColor = borderColor;
		this.autoSuggestionsPopUpWindow = autoSuggestor.getAutoSuggestionPopUpWindow();

		initComponent();
	}

	private void initComponent() {
		setFocusable(true);
		setForeground(suggestionsTextColor);
		setFocusTraversalKeysEnabled(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				super.mouseClicked(me);

				replaceWithSuggestedText();

				autoSuggestionsPopUpWindow.setVisible(false);
			}
		});

		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, true), "Tab released");
		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "Enter released");
		getActionMap().put("Enter released", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				replaceWithSuggestedText();
				autoSuggestionsPopUpWindow.setVisible(false);
			}
		});
		getActionMap().put("Tab released", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				replaceWithSuggestedText();
				autoSuggestionsPopUpWindow.setVisible(false);
			}
		});
	}

	public void setFocused(boolean focused) {
		if (focused) {
			setBorder(new LineBorder(suggestionBorderColor));
		} else {
			setBorder(null);
		}
		repaint();
		this.focused = focused;
	}

	public boolean isFocused() {
		return focused;
	}

	private void replaceWithSuggestedText() {
		String suggestedWord = getText();
		String text = textField.getText();
		String typedWord = autoSuggestor.getCurrentlyTypedWord();
		String t = text.substring(0, text.lastIndexOf(typedWord));
		String tmp = t + text.substring(text.lastIndexOf(typedWord)).replace(typedWord, suggestedWord);
		textField.setText(tmp + " ");
	}
}
