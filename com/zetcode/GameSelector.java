package com.zetcode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import acm.util.RandomGenerator;

@SuppressWarnings("serial")
public class GameSelector extends JFrame {
	
	/**Create spacing variables to be used throughout the class*/
	private static final Dimension VERT_SPACE = new Dimension(0, 10);
	private static final Dimension HORIZ_SPACE = new Dimension(5, 0);
	private static final String GAME_LIST_FOLDER_PATH = "/Users/tstewart/Desktop/RandomGame";
	/*private static final String GAME_LIST_FOLDER_PATH = "/Users/Molasses/Documents/Complete Programs/Game Selector";*/
	private static final File CONFIG = new File(GAME_LIST_FOLDER_PATH + "/Config.txt");
	
	/**
	 * Constructor for class
	 */
	public GameSelector() {
		initUI();
	}
	
	/**
	 * Creates the entire GUI
	 */
	private void initUI() {

		verifyConfigFileExists();
		
		verifyGameListExists();
		
		//Populate the gameList from the external file
		updateGameListVariable();
		ratingBias = getUseRatingBiasStateFromConfig();
		
		//Create the menu for the program
		JMenuBar menubar = createMenuBar();
		setJMenuBar(menubar);

		//Create the main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		getContentPane().add(mainPanel);
		
		//create the game shuffle panel
		gameShufflePanel = createGameShufflePanel();
		mainPanel.add(gameShufflePanel);
		
		//Create the Add Game Panel
		addGamePanel = new AddGamePanel();
		mainPanel.add(addGamePanel);
		
		//Create the Remove Game Panel
		removeGamePanel = createRemoveGamePanel();
		mainPanel.add(removeGamePanel);
		
		//Create the Rate Game Panel
		rateGamePanel = createRateGamePanel();
		mainPanel.add(rateGamePanel);
		
		changePanelInUse(gameShufflePanel);
		setWindowParameters();
		
	}

	private void verifyGameListExists() {
		csvFile = getFilePathFromConfig();
		if (fileDoesntExist(csvFile)) {
			displayMissingGameListErrorMessage();
			csvFile = newFileLocation();
		}
	}
	
	private void displayMissingGameListErrorMessage() {
		JOptionPane.showMessageDialog(this, "Game List not found.  " +
				"Please browse to and select the Game List.", 
				"Game List Not Found", JOptionPane.ERROR_MESSAGE);
	}

	private String newFileLocation() {
		FilePathFinder filePathFinder = new FilePathFinder(CONFIG);
		return filePathFinder.getPath();
		
	}

	private String getFilePathFromConfig() {
		BufferedReader pathReader = null;
		String gameListFilePath = "";
		try {
			pathReader = new BufferedReader(new FileReader(CONFIG));
			gameListFilePath = pathReader.readLine();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pathReader != null) {
				try {
					pathReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return gameListFilePath;
	}
	
	private boolean fileDoesntExist(String csvFile) {
		File file = new File(csvFile);
		return !file.exists();
	}
	
	/**
	 * Ensures there is a config file.  If the directory doesn't exist, it is created.
	 * If there is no config file, it will prompt for the game list file in order
	 * to populate the config file
	 */
	private void verifyConfigFileExists() {
		if (!CONFIG.exists()) {
			displayMissingGameListErrorMessage();
			String filePath = getFilePath();
			
			BufferedWriter writer;
			try {
				new File(GAME_LIST_FOLDER_PATH).mkdirs();
				writer = new BufferedWriter(new FileWriter(CONFIG));
				writer.write(filePath);
				writer.newLine();
				writer.write("Rating_Bias:true");
				writer.newLine();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Launches a file dialog to browse to the a file
	 * @return filePath the path to the file as String
	 */
	private String getFilePath() {
		JPanel panel = new JPanel();
		String filePath = null;
		JFileChooser fileopen = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("csv files", "csv");
		fileopen.setFileFilter(filter);
		int ret = fileopen.showDialog(panel, "Open Game List");
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileopen.getSelectedFile();
			filePath = file.getPath();
		}
		return filePath;
	}

	/**
	 * Populates the gameList instance variable with the list of
	 * games stored in the GameList.csv file
	 * @return List of all games with their number of players and type as ArrayList<String>
	 */
	private ArrayList<String> populateGameListFromFile() {
		ArrayList<String> gameList = new ArrayList<String>();

		BufferedReader br = null;
		String line = "";
		
		try{
			br = new BufferedReader(new FileReader(csvFile));
			while((line = br.readLine()) != null) {
				gameList.add(line);
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return gameList;
	}

	private class AddGamePanel extends JPanel {
		private AddGamePanel() {

			//Create the panel and set the layout
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			add(Box.createRigidArea(VERT_SPACE));

			JPanel addGameNameSubPanel = createAddGameNameSubPanel();
			add(addGameNameSubPanel);

			add(Box.createRigidArea(VERT_SPACE));

			JPanel addGameMinMaxPlayersSubPanel = createAddGameMinMaxPlayersSubPanel();
			add(addGameMinMaxPlayersSubPanel);

			add(Box.createRigidArea(VERT_SPACE));

			JLabel addGameTypeLabel = createAddGameTypeLabel();
			add(addGameTypeLabel);

			JPanel addGameTypeOptionsSubPanel = createAddGameTypeOptionsSubPanel();
			add(addGameTypeOptionsSubPanel);

			add(Box.createRigidArea(VERT_SPACE));

			JLabel addGameRatingLabel = createAddGameRatingLabel();
			add(addGameRatingLabel);

			JPanel addGameRatingOptionsSubPanel = createAddGameRatingOptionsSubPanel();
			add(addGameRatingOptionsSubPanel);

			add(Box.createVerticalGlue());

			JPanel addGameButtonsSubPanel = createAddGameButtonsSubPanel();
			add(addGameButtonsSubPanel);

			add(Box.createRigidArea(VERT_SPACE));

		}

		private JPanel createAddGameNameSubPanel() {
			JPanel namePanel = new JPanel();
			namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));

			JLabel gameNameLabel = new JLabel("Name of Game to add:");
			gameNameLabel.setFont(new Font("Serif", Font.BOLD, 14));
			namePanel.add(gameNameLabel);

			namePanel.add(Box.createRigidArea(HORIZ_SPACE));

			gameName = new JTextArea();
			gameName.setMaximumSize(new Dimension(150, 14));
			gameName.setFont(new Font("Sans", Font.PLAIN, 12));
			namePanel.add(gameName);
			return namePanel;		

		}

		private JPanel createAddGameMinMaxPlayersSubPanel() {

			JPanel minMaxPanel = new JPanel();
			minMaxPanel.setLayout(new BoxLayout(minMaxPanel, BoxLayout.X_AXIS));

			JLabel minPlayersLabel = new JLabel("Minimum # of Players");
			minPlayersLabel.setFont(new Font("Serif", Font.BOLD, 14));

			minPlayers = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
			minPlayers.setPreferredSize(new Dimension(50, 30));
			minPlayers.setMaximumSize(new Dimension(50, 30));

			JLabel maxPlayersLabel = new JLabel("Maximum # of Players");
			maxPlayersLabel.setFont(new Font("Serif", Font.BOLD, 14));

			maxPlayers = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
			maxPlayers.setPreferredSize(new Dimension(50, 30));
			maxPlayers.setMaximumSize(new Dimension(50, 30));

			minMaxPanel.add(minPlayersLabel);
			minMaxPanel.add(minPlayers);
			minMaxPanel.add(Box.createRigidArea(HORIZ_SPACE));
			minMaxPanel.add(maxPlayersLabel);
			minMaxPanel.add(maxPlayers);

			return minMaxPanel;

		}

		private JLabel createAddGameTypeLabel() {
			JLabel gameTypeLabel = new JLabel("Select the game type being added");
			gameTypeLabel.setFont(new Font("Serif", Font.BOLD, 14));
			gameTypeLabel.setAlignmentX(CENTER_ALIGNMENT);
			return gameTypeLabel;
		}

		private JPanel createAddGameTypeOptionsSubPanel() {
			JPanel gameTypePanel = new JPanel();
			gameTypePanel.setLayout(new BoxLayout(gameTypePanel, BoxLayout.X_AXIS));

			gameTypeButtonGroup = new ButtonGroup();

			JRadioButton board = new JRadioButton("Board", true);//board.setSelected(true);
			JRadioButton card = new JRadioButton("Card");
			JRadioButton dice = new JRadioButton("Dice");
			JRadioButton video = new JRadioButton("Video");
			JRadioButton kid = new JRadioButton("Kid");
			
			defaultGameTypeButtonModel = board.getModel();

			gameTypeButtonGroup.add(board);
			gameTypeButtonGroup.add(card);
			gameTypeButtonGroup.add(dice);
			gameTypeButtonGroup.add(video);
			gameTypeButtonGroup.add(kid);

			gameTypePanel.add(board);
			gameTypePanel.add(card);
			gameTypePanel.add(dice);
			gameTypePanel.add(video);
			gameTypePanel.add(kid);

			return gameTypePanel;
		}

		private JLabel createAddGameRatingLabel() {
			JLabel ratingLabel = new JLabel("Rate the game (hover ratings for descriptions.)");
			ratingLabel.setFont(new Font("Serif", Font.BOLD, 14));
			ratingLabel.setAlignmentX(CENTER_ALIGNMENT);
			return ratingLabel;
		}

		private JPanel createAddGameRatingOptionsSubPanel() {

			//Dimension starSize = new Dimension(70, 24);
			Font starFont = new Font("Sans", Font.PLAIN, 12);

			JPanel ratingPanel = new JPanel();
			ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.X_AXIS));

			ratingButtonGroup = new ButtonGroup();

			JRadioButton fiveStar = new JRadioButton("<html>" + starRating(5) + "</html>");
			fiveStar.setToolTipText("Love it! Play again right away!");
			fiveStar.setFont(starFont);
			//fiveStar.setPreferredSize(starSize);
			//fiveStar.setMaximumSize(starSize);
			fiveStar.setName("" + 5);

			JRadioButton fourStar = new JRadioButton("<html>" + starRating(4) + "</html>");
			fourStar.setToolTipText("Really like it!  Play again any time.");
			fourStar.setFont(starFont);
			//fourStar.setPreferredSize(starSize);
			//fourStar.setMaximumSize(starSize);
			fourStar.setName("" + 4);

			JRadioButton threeStar = new JRadioButton("<html>" + starRating(3) + "</html>");
			threeStar.setToolTipText("Liked it. Willing to play again");
			threeStar.setFont(starFont);
			//threeStar.setPreferredSize(starSize);
			//threeStar.setMaximumSize(starSize);
			threeStar.setName("" + 3);

			JRadioButton twoStar = new JRadioButton("<html>" + starRating(2) + "</html>");
			twoStar.setToolTipText("It's OK. Play again if someone else wants to.");
			twoStar.setFont(starFont);
			//twoStar.setPreferredSize(starSize);
			//twoStar.setMaximumSize(starSize);
			twoStar.setName("" + 2);

			JRadioButton oneStar = new JRadioButton("<html>" + starRating(1) + "</html>");
			oneStar.setToolTipText("LAME! Nuf Said.");
			oneStar.setFont(starFont);
			//oneStar.setPreferredSize(starSize);
			//oneStar.setMaximumSize(starSize);
			oneStar.setName("" + 1);

			JRadioButton notRated = new JRadioButton("None");
			notRated.setToolTipText("Game hasn't been played to be rated. Play ASAP and rate!");
			notRated.setFont(starFont);
			//notRated.setPreferredSize(starSize);
			//notRated.setMaximumSize(starSize);
			notRated.setSelected(true);
			notRated.setName("" + 0);
			
			defaultRatingButtonModel = notRated.getModel();

			ratingButtonGroup.add(fiveStar);
			ratingButtonGroup.add(fourStar);
			ratingButtonGroup.add(threeStar);
			ratingButtonGroup.add(twoStar);
			ratingButtonGroup.add(oneStar);
			ratingButtonGroup.add(notRated);

			ratingPanel.add(fiveStar);
			ratingPanel.add(fourStar);
			ratingPanel.add(threeStar);
			ratingPanel.add(twoStar);
			ratingPanel.add(oneStar);
			ratingPanel.add(notRated);

			return ratingPanel;
		}

		private JPanel createAddGameButtonsSubPanel() {
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

			JButton addGame = new JButton("Add Game");
			addGame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addGameToFile();
				}

			});

			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ReturnToGameShuffleActionListener());

			buttonPanel.add(addGame);
			buttonPanel.add(Box.createRigidArea(HORIZ_SPACE));
			buttonPanel.add(cancel);


			return buttonPanel;
		}

		public String getGameName() {
			return gameName.getText().trim();
		}
		
		/**
		 * Determines the rating of the game
		 * @return The rating of the game in stars (0 = not rated)
		 */
		public int getGameRating() {
			JRadioButton rating = getSelectedRadio(ratingButtonGroup);
			int ratingVal = Integer.parseInt(rating.getName());
			
			return ratingVal;
		}
		
		public String getGameType() {
			JRadioButton gameType = getSelectedRadio(gameTypeButtonGroup);
			return gameType.getText();
		}
		
		public int getMinPlayers() {
			return (Integer) minPlayers.getValue();
		}
		
		public int getMaxPlayers() {
			return (Integer) maxPlayers.getValue();
		}
		
		private JTextArea gameName;
		private ButtonGroup ratingButtonGroup;
		private ButtonGroup gameTypeButtonGroup;
		private JSpinner minPlayers;
		private JSpinner maxPlayers;

	}
	
	private void addGameToFile() {
		
		//If there is a game name then proceed with game addition
		if(gameNameIsUnique(addGamePanel.getGameName())) {
			if(gameAdditionIsConfirmed()) {
				addGame();
				updateGameListVariable();
				resetAddGamePanel();
				if(!addingAnotherGame()){
					changePanelInUse(gameShufflePanel);
				}
			}
		} else {
			//What happens if there is not a unique name?
			showNonUniqueGameErrorDialog();
		}
		
	}
	
	private void updateGameListVariable() {
		gameList = populateGameListFromFile();
	}

	private boolean addingAnotherGame() {
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, 
				"Would you like to add another game?", "Question", dialogButton);
		return dialogResult == JOptionPane.YES_OPTION;
	}

	private void showNonUniqueGameErrorDialog() {
		if(addGamePanel.getGameName().trim().equals("")) {
			JOptionPane.showMessageDialog(null, 
				"You need to enter a unique Game Name to continue.", "Error", JOptionPane.OK_OPTION);
		} else {
			JOptionPane.showMessageDialog(null, 
					"The game " + addGamePanel.getGameName() + " already exists.  " +
							"You need to enter a unique Game Name to continue.", "Error", JOptionPane.OK_OPTION);
		}
		
	}

	private void resetAddGamePanel() {
		addGamePanel.gameName.setText("");
		addGamePanel.maxPlayers.setValue(new Integer(2));
		addGamePanel.minPlayers.setValue(new Integer(1));
		addGamePanel.gameTypeButtonGroup.setSelected(defaultGameTypeButtonModel, true);
		addGamePanel.ratingButtonGroup.setSelected(defaultRatingButtonModel, true);
	}

	private boolean gameAdditionIsConfirmed() {
		String gameToAdd = "<html> Are you sure you want to add the following game?" +
				"<br><br>" + 
				"Game Name: " + addGamePanel.getGameName() + "<br>" +
				"Minimum Players: " + addGamePanel.getMinPlayers() + "<br>" +
				"Maximum Players: " + addGamePanel.getMaxPlayers() + "<br>" +
				"Game Type: " + addGamePanel.getGameType() + "<br>" +
				"Game Rating: " + addGamePanel.getGameRating() + " stars<br></html>";
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, 
				gameToAdd, "Question", dialogButton);
		return dialogResult == JOptionPane.YES_OPTION;
	}

	private void addGame() {
		String filePath = getFilePathFromConfig();
		File listFile = new File(filePath);
		File tempFile = new File(filePath.substring(0, filePath.length()-4) + 
				"_temp" + filePath.substring(filePath.length()-4));
		
		String gameToAdd = addGamePanel.getGameName() + "," +
				addGamePanel.getMinPlayers() + "," +
				addGamePanel.getMaxPlayers() + "," +
				addGamePanel.getGameType() + "," +
				addGamePanel.getGameRating();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(listFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			boolean gameAdded = false;
			while((currentLine = reader.readLine()) != null) {
				String[] currentArray = currentLine.split(",");
				if (!gameAdded &&
						currentArray[0].compareToIgnoreCase(addGamePanel.getGameName()) > 0) {
					writer.write(gameToAdd);
					writer.newLine();
					gameAdded = true;
				}
				writer.write(currentLine);
				writer.newLine();
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			listFile.delete();
			tempFile.renameTo(listFile);
		}
		
		
	}

	private boolean gameNameIsUnique(String name) {
		// Make sure there is a name entered
		if(name.trim().equals("")){
			return false;
		}
		//Make sure the name is unique when compared to the gameList
		for (int i = 0; i < gameList.size(); i++) {
			String[] gameData = gameList.get(i).split(",");
			if (name.equalsIgnoreCase(gameData[0])) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static JRadioButton getSelectedRadio(ButtonGroup group) {
	    for (Enumeration eRadio=group.getElements(); eRadio.hasMoreElements(); ) {
	        //Iterating over the Radio Buttons
	        JRadioButton radioButton = (JRadioButton)eRadio.nextElement();
	        //Comparing radioButtons model with groups selection
	        if (radioButton.getModel() == group.getSelection()) {
	            return radioButton;
	        }
	    }
	    return null;
	}
	
	private JPanel createRemoveGamePanel() {

		//Create the panel and set the layout
		JPanel removeGamePanel = new JPanel();
		removeGamePanel.setLayout(new BoxLayout(removeGamePanel, BoxLayout.Y_AXIS));

		//Create instructional label
		JLabel removeGameLabel = new JLabel("Select the game(s) to remove");
		removeGameLabel.setFont(new Font("Serif", Font.BOLD, 24));
		removeGameLabel.setAlignmentX(CENTER_ALIGNMENT);
		removeGameLabel.setAlignmentY(CENTER_ALIGNMENT);
		removeGamePanel.add(removeGameLabel);

		//Create listbox to make selection(s)
		removeGameList = new JList<String>();
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(removeGameList);
		Dimension listSize = new Dimension(300, 110);
		pane.setPreferredSize(listSize);
		pane.setMaximumSize(listSize);
		pane.setAlignmentX(CENTER_ALIGNMENT);
		removeGamePanel.add(pane);

		//Push buttons to the bottom of the screen
		removeGamePanel.add(Box.createVerticalGlue());


		//Create sub panel with Remove and Cancel buttons
		JPanel removeGameButtonPanel = new JPanel();
		removeGameButtonPanel.setLayout(new BoxLayout(removeGameButtonPanel, BoxLayout.X_AXIS));

		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(gameIsSelected()){
					removeSelectedGames();
				} else {
					displayNoGameSelectedErrorDialog();
				}	
			}
		});
		removeGameButtonPanel.add(remove);

		removeGameButtonPanel.add(Box.createRigidArea(HORIZ_SPACE));

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ReturnToGameShuffleActionListener());
		removeGameButtonPanel.add(cancel);

		removeGamePanel.add(removeGameButtonPanel);


		removeGamePanel.add(Box.createRigidArea(VERT_SPACE));

		return removeGamePanel;
	}
	
	protected void displayNoGameSelectedErrorDialog() {
		JOptionPane.showMessageDialog(null, 
				"No games are selected for removal.", "Error", JOptionPane.OK_OPTION);
	}

	private void removeSelectedGames() {
		List<String> listOfGamesToRemove = removeGameList.getSelectedValuesList();
		if(confirmDeletionOfSelectedGames(listOfGamesToRemove)) {
			deleteGames(listOfGamesToRemove);
			updateGameListVariable();
			changePanelInUse(gameShufflePanel);
		}
	}

	private void deleteGames(List<String> listOfGamesToRemove) {
		String filePath = getFilePathFromConfig();
		File listFile = new File(filePath);
		File tempFile = new File(filePath.substring(0, filePath.length()-4) + 
				"_temp" + filePath.substring(filePath.length()-4));
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(listFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				String[] currentArray = currentLine.split(",");
				boolean gameToDelete = false;
				for (int i = 0; i < listOfGamesToRemove.size(); i++) {
					if(currentArray[0].equals(listOfGamesToRemove.get(i))) {
						gameToDelete = true;
						break;
					}
				}
				if (!gameToDelete) {
					writer.write(currentLine);
					writer.newLine();
				}
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			listFile.delete();
			tempFile.renameTo(listFile);
		}
	}

	private boolean confirmDeletionOfSelectedGames(
			List<String> listOfGamesToRemove) {
		String gamesToDelete = "<html> Are you sure you want to delete the following game(s)?<br><br>";
		for(int i = 0; i < listOfGamesToRemove.size(); i ++) {
			gamesToDelete += listOfGamesToRemove.get(i) + "<br>";
		}
		
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, 
				gamesToDelete, "Question", dialogButton);
		return dialogResult == JOptionPane.YES_OPTION;
		
	}

	private boolean gameIsSelected() {
		return removeGameList.getSelectedIndex() != -1;
	}
	
	private JPanel createRateGamePanel() {
		
		rateGamePanel = new JPanel();
		rateGamePanel.setLayout(new BoxLayout(rateGamePanel, BoxLayout.X_AXIS));
		rateGamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		//Add JList with all games and their ratings
		rateGamePanel.add(createListOfGamesAndRatings());
		
		rateGamePanel.add(Box.createRigidArea(HORIZ_SPACE));
		
		// Add sub panel for all radio and regular buttons
		rateGamePanel.add(createRateGameButtonsSubPanel());
		
		return rateGamePanel;
	}


	private JPanel createRateGameButtonsSubPanel() {

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

		Dimension starSize = new Dimension(90, 20);
		Font starFont = new Font("Sans", Font.PLAIN, 12);
		
		JRadioButton fiveStar = new JRadioButton("<html>" + starRating(5) + "</html>");
		fiveStar.setToolTipText("Love it! Play again right away!");
		fiveStar.setFont(starFont);
		fiveStar.setPreferredSize(starSize);
		fiveStar.setMaximumSize(starSize);
		fiveStar.setName("" + 5);

		JRadioButton fourStar = new JRadioButton("<html>" + starRating(4) + "</html>");
		fourStar.setToolTipText("Really like it!  Play again any time.");
		fourStar.setFont(starFont);
		fourStar.setPreferredSize(starSize);
		fourStar.setMaximumSize(starSize);
		fourStar.setName("" + 4);

		JRadioButton threeStar = new JRadioButton("<html>" + starRating(3) + "</html>");
		threeStar.setToolTipText("Liked it. Willing to play again");
		threeStar.setFont(starFont);
		threeStar.setPreferredSize(starSize);
		threeStar.setMaximumSize(starSize);
		threeStar.setName("" + 3);

		JRadioButton twoStar = new JRadioButton("<html>" + starRating(2) + "</html>");
		twoStar.setToolTipText("It's OK. Play again if someone else wants to.");
		twoStar.setFont(starFont);
		twoStar.setPreferredSize(starSize);
		twoStar.setMaximumSize(starSize);
		twoStar.setName("" + 2);

		JRadioButton oneStar = new JRadioButton("<html>" + starRating(1) + "</html>");
		oneStar.setToolTipText("LAME! Nuf Said.");
		oneStar.setFont(starFont);
		oneStar.setPreferredSize(starSize);
		oneStar.setMaximumSize(starSize);
		oneStar.setName("" + 1);

		JRadioButton notRated = new JRadioButton("None");
		notRated.setToolTipText("Game hasn't been played to be rated. Play ASAP and rate!");
		notRated.setFont(starFont);
		notRated.setPreferredSize(starSize);
		notRated.setMaximumSize(starSize);
		notRated.setSelected(true);
		notRated.setName("" + 0);
		
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Get new rating
				int newRating = getNewRating();
				
				//Get the game name
				int newRatingIndex = gamesAndRatings.getSelectedIndex();
				String newRatingGame = gameList.get(newRatingIndex).split(",")[0];
				
				//Update the file
				updateFileWithNewRating(newRatingGame, newRating);
				
				//Update the list
				updateGameListVariable();
				updateGamesAndRatingsList();
				
			}

			private int getNewRating() {
				ButtonModel newRatingModel = gameRatingPanelButtonGroup.getSelection();
				int newRating = -1;
				for (int i = 0; i < gameRatingPanelButtonGroupModels.length; i++) {
					if (gameRatingPanelButtonGroupModels[i].equals(newRatingModel)) {
						newRating = i;
					}
				}
				return newRating;
			}
			
		});
		
		JButton done = new JButton("Done");
		done.addActionListener(new ReturnToGameShuffleActionListener());
		
		gameRatingPanelButtonGroup = new ButtonGroup();
		gameRatingPanelButtonGroup.add(fiveStar);
		gameRatingPanelButtonGroup.add(fourStar);
		gameRatingPanelButtonGroup.add(threeStar);
		gameRatingPanelButtonGroup.add(twoStar);
		gameRatingPanelButtonGroup.add(oneStar);
		gameRatingPanelButtonGroup.add(notRated);
		
		gameRatingPanelButtonGroupModels = new ButtonModel[6];
		gameRatingPanelButtonGroupModels[0] = notRated.getModel();
		gameRatingPanelButtonGroupModels[1] = oneStar.getModel();
		gameRatingPanelButtonGroupModels[2] = twoStar.getModel();
		gameRatingPanelButtonGroupModels[3] = threeStar.getModel();
		gameRatingPanelButtonGroupModels[4] = fourStar.getModel();
		gameRatingPanelButtonGroupModels[5] = fiveStar.getModel();
		
		buttonPanel.add(fiveStar);
		buttonPanel.add(fourStar);
		buttonPanel.add(threeStar);
		buttonPanel.add(twoStar);
		buttonPanel.add(oneStar);
		buttonPanel.add(notRated);
		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(save);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		buttonPanel.add(done);
		
		return buttonPanel;
	}

	protected void updateFileWithNewRating(String newRatingGame, int newRating) {
		String filePath = getFilePathFromConfig();
		File listFile = new File(filePath);
		File tempFile = new File(filePath.substring(0, filePath.length()-4) + 
				"_temp" + filePath.substring(filePath.length()-4));
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(listFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				String[] currentArray = currentLine.split(",");
				if (currentArray[0].equalsIgnoreCase(newRatingGame)) {
					String gameWithNewRatingString = "";
					for (int i = 0; i < currentArray.length - 1; i++ ) {
						gameWithNewRatingString += currentArray[i] + ",";
					}
					gameWithNewRatingString += newRating;
					writer.write(gameWithNewRatingString);
					writer.newLine();
				} else {
					writer.write(currentLine);
					writer.newLine();
				}
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			listFile.delete();
			tempFile.renameTo(listFile);
		}
		
	}

	private JScrollPane createListOfGamesAndRatings() {
		
		gamesAndRatings = new JList<String>();
		updateGamesAndRatingsList();
		gamesAndRatings.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int index = gamesAndRatings.getSelectedIndex();
					if (index >=0) {
						String[] selectedGame = gameList.get(index).split(",");
						int selectedGameRating = Integer.valueOf(selectedGame[selectedGame.length - 1]);
						updateRatingRadioButtons(selectedGameRating);
					}
				}
			}
		});
		
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(gamesAndRatings);
		pane.setPreferredSize(new Dimension(275, 175));
		pane.setMaximumSize(new Dimension(275, 175));
		return pane;
	}


	protected void updateRatingRadioButtons(int selectedGameRating) {
		gameRatingPanelButtonGroup.setSelected(gameRatingPanelButtonGroupModels[selectedGameRating], true);
	}

	private void updateGamesAndRatingsList() {
		
		
		gamesAndRatings.setListData(completeListWithRatings());
	}


	private String[] completeListWithRatings() {
		String[] list = new String[gameList.size()];

		for(int i = 0; i < gameList.size(); i++) {
			String[] currentGame = gameList.get(i).split(",");
			list[i] = "<html>" + currentGame[0] + " " + starRating(Integer.valueOf(currentGame[currentGame.length-1])) + "</html>";
		}
		return list;
	}

	private String starRating(Integer starCount) {
		String stars = "";
		for (int i = 0; i < starCount; i++) {
			stars += "&#9733"; 
		}
		return stars;
	}


	/**
	 * Creates gameTypeOptionsPanel by extending JPanel
	 * Private class for the game options panel so the isSelected method
	 * can be applied to all four options without having to store them as
	 * an instance variable
	 * @author tstewart
	 *
	 */
	private class GameTypeOptionsPanel extends JPanel {
		
		public GameTypeOptionsPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setAlignmentX(CENTER_ALIGNMENT);
			
			//Add Check boxes for different game types
			board = new JCheckBox("Board", true);
			board.setFont(new Font("Sans", Font.BOLD, 14));
			board.setAlignmentX(CENTER_ALIGNMENT);
			board.setFocusable(false);
			
			card = new JCheckBox("Card", true);
			card.setFont(new Font("Sans", Font.BOLD, 14));
			card.setAlignmentX(CENTER_ALIGNMENT);
			card.setFocusable(false);
			
			dice = new JCheckBox("Dice", true);
			dice.setFont(new Font("Sans", Font.BOLD, 14));
			dice.setAlignmentX(CENTER_ALIGNMENT);
			dice.setFocusable(false);
			
			video = new JCheckBox("Video", true);
			video.setFont(new Font("Sans", Font.BOLD, 14));
			video.setAlignmentX(CENTER_ALIGNMENT);
			video.setFocusable(false);
			
			kid = new JCheckBox("Kid", false);
			kid.setFont(new Font("Sans", Font.BOLD, 14));
			kid.setAlignmentX(CENTER_ALIGNMENT);
			kid.setFocusable(false);
			
			add(board);
			add(card);
			add(dice);
			add(video);
			add(kid);
		}
		
		public boolean isBoardSelected() {
			return board.isSelected();
		}
		
		public boolean isCardSelected() {
			return card.isSelected();
		}
		
		public boolean isDiceSelected() {
			return dice.isSelected();
		}
		
		public boolean isVideoSelected() {
			return video.isSelected();
		}
		
		public boolean isKidSelected() {
			return kid.isSelected();
		}
		
		
		private JCheckBox board;
		private JCheckBox card;
		private JCheckBox dice;
		private JCheckBox video;
		private JCheckBox kid;
		
	}
	
	/**
	 * Creates the main Game Shuffle panel where random games will be selected
	 * @return gameShufflePanel as JPanel
	 */
	private JPanel createGameShufflePanel() {
		
		//Create the game shuffle panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		//Add playerCountPanel to the main panel
		JPanel playerCountSubPanel = createPlayerCountSubPanel();
		panel.add(playerCountSubPanel);
		
		//Create the label to display the game to play
		selectedGame = createSelectedGameLabel();
		panel.add(selectedGame);
		
		//Add glue to push the rest of the contents to the bottom of the panel
		panel.add(Box.createVerticalGlue());
		
		//Create the game type options panel
		gameTypeOptionsPanel = new GameTypeOptionsPanel();
		panel.add(gameTypeOptionsPanel);
		
		//Create a getGame panel
		JPanel getGameSubPanel = createGetGameSubPanel();
		panel.add(getGameSubPanel);
		
		//Add space between the sub panels
		panel.add(Box.createRigidArea(VERT_SPACE));
		
		//Create the EditListButtonsSubPanel
		JPanel editListButtonsSubPanel = createEditListButtonsSubPanel();
		
		panel.add(editListButtonsSubPanel);
		
		//Add space to the bottom of the panel
		panel.add(Box.createRigidArea(VERT_SPACE));
		
		return panel;
	}

	/**
	 * Creates the editListSubPanel as a JPanel
	 * @return editListSubPanel as JPanel
	 */
	private JPanel createEditListButtonsSubPanel() {
				
		//Get the icons needed for the buttons
		icon iconAddGame = new icon("/icons/Add Game.jpg");
		icon iconRemoveGame = new icon("/icons/Recycle Bin.png");
		icon iconExit = new icon("/icons/Exit.png");
		
		//Create the panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		//Create the buttons for the panel
		JButton addNewGame = new JButton("Add Game", iconAddGame);
		addNewGame.setToolTipText("Add a game to the list of games.");
		addNewGame.addActionListener(new GoToAddGamePanelActionListener());
		
		JButton removeOldGame = new JButton("Remove Game", iconRemoveGame);
		removeOldGame.setToolTipText("Remove a game from the list of games.");
		removeOldGame.addActionListener(new GoToRemoveGamePanelActionListener());
		
		JButton exitButton = new JButton("Exit", iconExit);
		exitButton.setToolTipText("Exit Application");
		exitButton.addActionListener(new ExitActionListener());
		
		
		buttonPanel.add(Box.createRigidArea(HORIZ_SPACE));
		buttonPanel.add(addNewGame);
		buttonPanel.add(Box.createRigidArea(HORIZ_SPACE));
		buttonPanel.add(removeOldGame);
		buttonPanel.add(Box.createRigidArea(HORIZ_SPACE));
		buttonPanel.add(exitButton);
		buttonPanel.add(Box.createRigidArea(HORIZ_SPACE));
		return buttonPanel;
	}

	/**
	 * creates the subPanel with the buttons to select a game to play
	 * @return
	 */
	private JPanel createGetGameSubPanel() {

		//Get the icons needed for the buttons
		icon iconChooseGame = new icon("/icons/Monopoly Board.png");
		icon iconGetListOfGames = new icon("/icons/Games Folder.png");
		
		//Create the panel
		JPanel getGamePanel = new JPanel();
		getGamePanel.setLayout(new BoxLayout(getGamePanel, BoxLayout.X_AXIS));
		getGamePanel.setAlignmentX(CENTER_ALIGNMENT);
		
		//Add the buttons to the getGame panel
		JButton chooseGame = new JButton("Choose Game", iconChooseGame);
		chooseGame.setAlignmentX(CENTER_ALIGNMENT);
		chooseGame.setToolTipText("<html>Get a randomly chosen game for<br>" +
				"the selected number of players.</html>");
		chooseGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedGame.setText(getRandomGame());
			}
		});
		
		JButton getListOfGames = new JButton("Get List of Games", iconGetListOfGames);
		getListOfGames.setAlignmentX(CENTER_ALIGNMENT);
		getListOfGames.setToolTipText("<html>Displays a list of games that<br>" +
				"can be played by the selected<br>" +
				"number of players</html>");
		getListOfGames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayList();
			}
		});
		
		getGamePanel.add(chooseGame);
		getGamePanel.add(Box.createRigidArea(HORIZ_SPACE));
		getGamePanel.add(getListOfGames);
		
		return getGamePanel;
	}
	
	/**
	 * Creates the JLabel that will display the randomly chosen game
	 * @return JLabel to display name of randomly chosen game.
	 */
	private JLabel createSelectedGameLabel() {
		JLabel label = new JLabel("Select Option Below");
		label.setFont(new Font("Serif", Font.BOLD, 24));
		label.setAlignmentX(CENTER_ALIGNMENT);
		label.setAlignmentY(CENTER_ALIGNMENT);
		return label;
	}
	
	/**
	 * creates the player count sub panel
	 * @return playerCountSubPanel as JPanel
	 */
	private JPanel createPlayerCountSubPanel() {
		//Create sub-panel for getting Player Count
		JPanel playerCountPanel = new JPanel();
		playerCountPanel.setLayout(new BoxLayout(playerCountPanel, BoxLayout.X_AXIS));
		playerCountPanel.setAlignmentX(CENTER_ALIGNMENT);
		
		//Create the label for getting player count
		JLabel playerCountLabel = new JLabel("Select the number of players:");
		playerCountLabel.setFont(new Font("Sans", Font.BOLD, 14));
		playerCountPanel.add(playerCountLabel);
		playerCountPanel.add(Box.createRigidArea(HORIZ_SPACE));
		
		//Add the playerCount spinner to the playerCountPanel
		playerCount = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
		playerCount.setPreferredSize(new Dimension(50, 30));
		playerCount.setMaximumSize(new Dimension(50, 30));
		playerCountPanel.add(playerCount);
		return playerCountPanel;
	}

	private void setWindowParameters() {
		setTitle("Game Selector");
		setSize(415, 260);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Creates the menu bar for the application
	 * @return Returns the menu bar as JMenuBar
	 */
	private JMenuBar createMenuBar() {
		
		JMenuBar menubar = new JMenuBar();
		
		//Create the menus
		JMenu fileMenu = createFileMenu();
		JMenu preferencesMenu = createPreferencesMenu();
		JMenu gamesMenu = createGamesMenu();
		JMenu helpMenu = createHelpMenu();
		
		//Add the menus to the menubar
		menubar.add(fileMenu);
		menubar.add(preferencesMenu);
		menubar.add(gamesMenu);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(helpMenu);
		
		return menubar;
	}

	/**
	 * Creates the file menu for the menu bar
	 * @return Returns a "File" menu as a JMenu for the Menu Bar
	 */
	private JMenu createFileMenu() {
		//Get the icons for the menu items
		icon iconAddGame = new icon("/icons/Add Game.jpg");
		icon iconRemoveGame = new icon("/icons/Recycle Bin.png");
		icon iconExit = new icon("/icons/Exit.png");
		
		//Create the menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		//Create the items for the menu
		JMenuItem addGame = new JMenuItem("Add Game", iconAddGame);
		addGame.setMnemonic(KeyEvent.VK_A);
		addGame.setToolTipText("Add a game to the list of games.");
		addGame.addActionListener(new GoToAddGamePanelActionListener());
		
		JMenuItem removeGame = new JMenuItem("Remove Game", iconRemoveGame);
		removeGame.setMnemonic(KeyEvent.VK_R);
		removeGame.setToolTipText("Remove a game from the list of games.");
		removeGame.addActionListener(new GoToRemoveGamePanelActionListener());
		
		JMenuItem exit = new JMenuItem("Exit", iconExit);
		exit.setMnemonic(KeyEvent.VK_E);
		exit.setToolTipText("Exit Application");
		exit.addActionListener(new ExitActionListener());
		
		//Add the menu items to the menu
		fileMenu.add(addGame);
		fileMenu.add(removeGame);
		fileMenu.add(exit);
		
		return fileMenu;
	}

	private JMenu createPreferencesMenu() {
		//Create the menu
		JMenu preferencesMenu = new JMenu("Preferences");
		preferencesMenu.setMnemonic(KeyEvent.VK_P);
		
		//Create the items for the menu
		final JCheckBoxMenuItem useRatingBias = new JCheckBoxMenuItem("Use Rating Bias");
		useRatingBias.setToolTipText("Play higher rated games more often");
		useRatingBias.setState(ratingBias);
		
		useRatingBias.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ratingBias = useRatingBias.getState();
				updateRatingBiasConfig();
			}
		});
		
		//Add the menu items to the menu
		preferencesMenu.add(useRatingBias);
		
		return preferencesMenu;
	}

	protected void updateRatingBiasConfig() {
		File configFile = CONFIG;
		String filePath = configFile.getPath();
		File tempFile = new File(filePath.substring(0, filePath.length()-4) + 
				"_temp" + filePath.substring(filePath.length()-4));
		
		String ratingBiasState = "Rating_Bias:" + ratingBias;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			String currentLine;
			while((currentLine = reader.readLine()) != null) {
				if (!currentLine.contains("Rating_Bias")) {
					writer.write(currentLine);
					writer.newLine();
				} else {
					writer.write(ratingBiasState);
					writer.newLine();
				}
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			configFile.delete();
			tempFile.renameTo(configFile);
		}
		
	}

	private boolean getUseRatingBiasStateFromConfig() {
		BufferedReader pathReader = null;
		String currentLine = "";
		boolean useBias = false;
		try {
			pathReader = new BufferedReader(new FileReader(CONFIG));
			
			while((currentLine = pathReader.readLine()) != null) {
				if(currentLine.contains("Rating_Bias")) {
					useBias = currentLine.contains("true");
				}
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pathReader != null) {
				try {
					pathReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return useBias;
	}

	private JMenu createGamesMenu() {
		//Create the menu
		JMenu gamesMenu = new JMenu("Games");
		gamesMenu.setMnemonic(KeyEvent.VK_G);
		
		//Create the items for the menu
		JMenuItem rateGames = new JMenuItem("Rate Games");
		rateGames.setMnemonic(KeyEvent.VK_R);
		rateGames.addActionListener(new GoToRateGamePanelActionListener());
		
		JMenuItem viewAllGames = new JMenuItem("View All Games");
		viewAllGames.setMnemonic(KeyEvent.VK_V);
		viewAllGames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				displayFullList();
			}
		});
		
		//Add the menu items to the menu
		gamesMenu.add(rateGames);
		gamesMenu.add(viewAllGames);
		
		return gamesMenu;
	}
	
	/**
	 * Creates the help menu for the menu bar
	 * @return Returns a "Help" menu as a JMenu for the Menu Bar
	 */
	private JMenu createHelpMenu() {
		
		//Get the icons for the help menu
		icon iconAbout = new icon("/icons/About.jpg");
		icon iconGettingStarted = new icon("/icons/Start.png");
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		//Create the items for the help menu
		JMenuItem gettingStarted = new JMenuItem("Getting Started", iconGettingStarted);
		gettingStarted.setMnemonic(KeyEvent.VK_G);
		//TODO Add ActionListener
		
		JMenuItem about = new JMenuItem("About", iconAbout);
		about.setMnemonic(KeyEvent.VK_B);
		//TODO Add ActionListener
		
		
		//Add the help menu items to the help menu
		helpMenu.add(gettingStarted);
		helpMenu.add(about);
		
		return helpMenu;
	}
	
	/**
	 * Creates an ImageIcon object
	 * @author tstewart
	 * 
	 */
	private class icon extends ImageIcon {
		/**
		 * Constructor for class.  
		 * Changes Image to be the one specified by the path
		 * 
		 * Images need to be stored within the project. 
		 * 
		 * @param iconPath Path top the image to use for the icon.
		 */
		private icon(String iconPath) {
			ImageIcon image = new ImageIcon(GameSelector.class.getResource(iconPath));
			setImage(image.getImage());
		}
	}

	/**
	 * Generates the random game to play
	 * @return Random Game as String
	 */
	private String getRandomGame() {
		ArrayList<String> availableGames = getListOfAvailableGames();
		if (availableGames.size()==0) {
			return "No Games Available";
		} else {
			if(ratingBias) {
				return selectGameWithRatingBias(availableGames);
			} else {
				return selectGameWithoutRatingBias(availableGames);
			}
		}
	}
	
	private String selectGameWithRatingBias(ArrayList<String> availableGames) {

		int index = -1;
		
		//Make list of ratings that matches list of games
		Integer gameRatings[] = new Integer[availableGames.size()];
		for(int i = 0; i < availableGames.size(); i++) {
			String currentGame = availableGames.get(i);
			for(int j = 0; j < gameList.size(); j++) {
				String gameMaster[] = gameList.get(j).split(",");
				if(gameMaster[0].equalsIgnoreCase(currentGame)) {
					gameRatings[i] = Integer.parseInt(gameMaster[4]);
					break;
				}
			}
		}
		
		//Sum ratings
		int ratingSum = 0;
		for(int i = 0; i < gameRatings.length; i++) {
			if (gameRatings[i] == 0) {
				ratingSum += 5;
			} else if (gameRatings[i] == 5) {
				ratingSum += 6;
			} else {
				ratingSum += gameRatings[i];
			}
		}
		
		//Get random number
		RandomGenerator rgen = RandomGenerator.getInstance();
		double rnd = rgen.nextDouble(0, ratingSum);
		
		//iterate through list of ratings to get index number
		for(int i = 0; i < gameRatings.length; i++) {
			if (gameRatings[i] == 0) {
				rnd -= 5;
			} else if (gameRatings[i] == 5) {
				rnd -= 6;
			} else {
				rnd -= gameRatings[i];
			}
			if(rnd <= 0) {
				index = i;
				break;
			}
		}
		
		return availableGames.get(index);
	}

	private String selectGameWithoutRatingBias(ArrayList<String> availableGames) {
		RandomGenerator rgen = RandomGenerator.getInstance();
		int index = rgen.nextInt(0, availableGames.size() - 1);
		return availableGames.get(index);
	}

	/**
	 * Generates a list of games available to play based on
	 * the number of players selected and the type options selected
	 * @return listOfAvailableGames as ArrayList<String>
	 */
	private ArrayList<String> getListOfAvailableGames() {
		String cvsSplitBy = ",";
		ArrayList<String> list = new ArrayList<String>();
		
		for(int i = 0; i < gameList.size(); i++) {
			String[] game = gameList.get(i).split(cvsSplitBy);
			boolean validGame = Integer.valueOf(game[1]) <= (Integer)playerCount.getValue()
				&& Integer.valueOf(game[2]) >= (Integer)playerCount.getValue()
				&& (
						(gameTypeOptionsPanel.isBoardSelected() && game[3].equals("Board")) ||
						(gameTypeOptionsPanel.isCardSelected() && game[3].equals("Card")) ||
						(gameTypeOptionsPanel.isDiceSelected() && game[3].equals("Dice")) ||
						(gameTypeOptionsPanel.isVideoSelected() && game[3].equals("Video")) ||
						(gameTypeOptionsPanel.isKidSelected() && game[3].equals("Kid")));
			if (validGame) {
				/*list.add("<html>" + game[0] + " " + starRating(Integer.valueOf(game[game.length-1])) + "</html>");*/
				list.add(game[0]);
			}
			
		}
		return list;
	}

	/**
	 * Displays the list of games available
	 */
	private void displayList() {
				
		final JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		
		String[] modifiedList = getListOfGamesforDisplay();
		for (int i = 0; i < modifiedList.length; i++) {
			modifiedList[i] = "<html>" + modifiedList[i] + " " + starRating(ratingOfGame(modifiedList[i])) + "</html>";
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JList list = new JList(modifiedList);
		
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(list);
		
		pane.setPreferredSize(new Dimension(300, 200));
		
		panel.add(new JLabel("<html>This is a list of games available for " +
				                   "the specified number of players.<br><br></html>"), BorderLayout.NORTH);
		panel.add(pane);
		JButton ok = new JButton("OK");
		ok.addActionListener(new DisposeActionListener(frame));
		panel.add(ok, BorderLayout.SOUTH);
		
		frame.add(panel);
				
		frame.pack();
		frame.setSize(new Dimension(300, 350));
		frame.setTitle("List of Available Games");
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
	}
	
	private Integer ratingOfGame(String game) {
		int rating = 0;
		for (int i = 0; i < gameList.size(); i++) {
			String[] currentLine = gameList.get(i).split(",");
			if (game.equalsIgnoreCase(currentLine[0])) {
				rating = Integer.valueOf(currentLine[currentLine.length - 1]);
				break;
			}
		}
		return rating;
	}

	/**
	 * Converts the list of available games to play from an ArrayList to an Array
	 * @return List of Available games as String[]
	 */
	private String[] getListOfGamesforDisplay() {
		
		ArrayList<String> availableGamesArray = getListOfAvailableGames();

		String[] list = new String[availableGamesArray.size()];
		for (int i = 0; i < availableGamesArray.size(); i++) {
			list[i] = availableGamesArray.get(i);
		}

		return list;
	}
	
	
	private void displayFullList() {
		final JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JLabel fullGameList = new JLabel(getFullGameListHTML());
		
		JScrollPane pane = new JScrollPane();
		pane.getViewport().add(fullGameList);
		pane.setAlignmentX(CENTER_ALIGNMENT);
		pane.setPreferredSize(new Dimension(500, 224));
		
		panel.add(pane);
		JButton ok = new JButton("OK");
		ok.addActionListener(new DisposeActionListener(frame));
		ok.setPreferredSize(new Dimension(200, 25));
		ok.setMaximumSize(new Dimension(200, 25));
		ok.setAlignmentX(CENTER_ALIGNMENT);
		
		panel.add(Box.createVerticalGlue());
		
		panel.add(ok);
		
		frame.add(panel);
				
		frame.pack();
		frame.setSize(new Dimension(640, 350));
		frame.setTitle("Full List of Games");
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private String getFullGameListHTML() {

		String gameListHTML = fullListTablePreamble();
		
		gameListHTML += createTableHeaders();
		
		gameListHTML = addAllGamesHTML(gameListHTML);
		
		gameListHTML += finishTable();
		
		return gameListHTML;
	}

	private String addAllGamesHTML(String gameListHTML) {
		for(int i = 0; i < gameList.size(); i++) {
			String[] currentGame = gameList.get(i).split(",");
			gameListHTML += startTableRow();
			gameListHTML += "<td>" + (i+1) + "</td>";
			for (int j = 0; j < currentGame.length; j++) {
				if(j != currentGame.length - 1) {
					gameListHTML += "<td>" + currentGame[j] + "</td>";
				} else {
					if(Integer.valueOf(currentGame[j]) == 0) {
						gameListHTML += "<td>Not Rated</td>";
					} else {
						gameListHTML += "<td>" + 
								starRating(Integer.valueOf(currentGame[j])) + 
								"</td>";
					}
				}
			}
			gameListHTML += endTableRow();
		}
		
		return gameListHTML;
	}

	private String createTableHeaders() {
		String headers = startTableRow();
		
		headers += addHeader("ID");
		headers += addHeader("Game Name");
		headers += addHeader("Min Players");
		headers += addHeader("Max Players");
		headers += addHeader("Game Type");
		headers += addHeader("Game Rating");
		
		headers += endTableRow();
		
		return headers;
	}

	private String addHeader(String string) {
		return "<th>" + string + "</th>";
	}

	private String startTableRow() {
		return "<tr text-align=\"center\">";
	}
	
	private String endTableRow() {
		return "</tr>";
	}

	private String finishTable() {
		return "</table></html>";
	}

	private String fullListTablePreamble() {
		String preamble = 
				"<html>" +
				"<style type=\"text/css\">" +
				".tftable {font-size:9px;color:#333333;width:100%;border-width: 1px;" +
				"border-color: #729ea5;border-collapse: collapse;}" +
				".tftable th {font-size:9px;background-color:#acc8cc;border-width: 1px;" +
				"padding: 8px;border-style: solid;border-color: #729ea5;text-align:left;}" +
				".tftable tr {background-color:#d4e3e5;}" +
				".tftable td {font-size:9px;border-width: 1px;padding: 8px;" +
				"border-style: solid;border-color: #729ea5;}" +
				"</style>" +
				"<table class=\"tftable\" border=\"1\">";
		return preamble;
	}

	/**
	 * Changes the visibility of all the sub panels within the
	 * mainPanel so the panelToUse is the only one visible
	 * 
	 * @param panelToUse The JPanel to make visible
	 */
	private void changePanelInUse(JPanel panelToUse) {
		for (Component c : mainPanel.getComponents()) {
			if (c instanceof JPanel) {
				if (c == panelToUse) {
					c.setVisible(true);
				} else
					c.setVisible(false);
			}
		}
	}

	/**
	 * Action listener for exiting the program
	 * @author tstewart
	 *
	 */
	private class ExitActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	/**
	 * ActionListener for returning to the gameShufflePanel
	 * @author tstewart
	 *
	 */
	private class ReturnToGameShuffleActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			changePanelInUse(gameShufflePanel);
		}
	}
	
	/**
	 * ActionListener to go to the Add Game Panel
	 * @author tstewart
	 *
	 */
	private class GoToAddGamePanelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			changePanelInUse(addGamePanel);
		}
	}
	
	/**
	 * ActionListener to go to the Remove Game Panel
	 * @author tstewart
	 *
	 */
	private class GoToRemoveGamePanelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			removeGameList.setListData(completeList());
			changePanelInUse(removeGamePanel);
		}

		private String[] completeList() {
			String[] completeList = new String[gameList.size()];
			for (int i = 0; i < completeList.length; i++) {
				completeList[i] = gameList.get(i).split(",")[0];
			}
			return completeList;
		}
	}
	
	/**
	 * ActionListener to go to the Rate Game Panel
	 * @author tstewart
	 *
	 */
	private class GoToRateGamePanelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			changePanelInUse(rateGamePanel);
		}
	}
	
	/**
	 * ActionListener to dispose a JFrame
	 * @author tstewart
	 */
	private class DisposeActionListener implements ActionListener {
		public DisposeActionListener(JFrame frame) {
			frameToClose = frame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			frameToClose.dispose();
			
		}
		JFrame frameToClose;
	}
	
	/**
	 * Main method which starts the program
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GameSelector ex = new GameSelector();
				ex.setVisible(true);
			}
		});

	}
	
	//Instance variables
	private JPanel mainPanel;
	private JPanel gameShufflePanel;
	private JPanel rateGamePanel;
	private AddGamePanel addGamePanel;
	private ButtonModel defaultGameTypeButtonModel;
	private ButtonModel defaultRatingButtonModel;
	private JPanel removeGamePanel;
	private GameTypeOptionsPanel gameTypeOptionsPanel;
	private JSpinner playerCount;
	private JLabel selectedGame;
	private ArrayList<String> gameList;
	private String csvFile;
	private ButtonGroup gameRatingPanelButtonGroup;
	private ButtonModel[] gameRatingPanelButtonGroupModels;
	private boolean ratingBias;
	public JList<String> removeGameList;
	private JList<String> gamesAndRatings;
}
