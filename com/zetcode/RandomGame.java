package com.zetcode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import acm.util.*;

public class RandomGame {
	
	public RandomGame(int playerCount) {
		selectRandomGame(playerCount);
	}
	
	public RandomGame(int playerCount, boolean makeListOfGames) {
		if (makeListOfGames) {
			listOfGames = getListOfGames(playerCount);
		} else {
			selectRandomGame(playerCount);
		}
	}
	
	private ArrayList<String> getListOfGames(int playerCount) {
		String csvFile = "./documents/GameList";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		ArrayList<String>gameArray = new ArrayList<String>();
		
		try{
			br = new BufferedReader(new FileReader(csvFile));
			while((line = br.readLine()) != null) {
				String[] game = line.split(cvsSplitBy);
				if (Integer.valueOf(game[1]) <= playerCount && 
						Integer.valueOf(game[2]) >= playerCount) {
					gameArray.add(game[0]);
				}
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
		
		return gameArray;
		
	}

	private void selectRandomGame(int playerCount) {
		
		ArrayList<String> gameArray = getListOfGames(playerCount);
		
		RandomGenerator rgen = RandomGenerator.getInstance();
		int index = rgen.nextInt(0, gameArray.size() - 1);
		gameName = gameArray.get(index);		
	}

	public String getName() {
		return gameName;
	}
	
	public ArrayList<String> getListOfGames() {
		return listOfGames;
	}
	
	
	private String gameName;
	private ArrayList<String> listOfGames;
}
