# RandomGameSelector
The Random Game Selector is a Java based program that enables poor decision makers to be able to play a game without having to deal with actually selecting a game themselves. The Random Game Selector will perform that menial task for them based on a few simple parameter selections: number of people playing and the desired type(s) of games (e.g. board, card, dice, video, and kid) to play. Also, the selection will be weighted by the rating of the game on a 1-5 star system.

In order for the selection process to work, the user must populate a sudo-database (CSV file) with all of their games meta data. This process can either be done in a program like Exel where the user can utilize operations like copy and paste to speed up the process of entering many games. The program also allows editing the CSV file from within the program allowing the user to easily add a new game, delete an old game, or update that metadata for an existing game (most commonly used to update the ratings).

#Background
After several months of learning to code by utilizing an Introduction to Programming course posted to iTunes University by Stanford, I decided it was time to use my new knowledge on a home-grown project. I decided to make this game selector because my Wife and I both dislike selecting a game to play. I also dislike playing the same game all the time, so having a game selected randomly has a lot of appeal.

#Project Highlight
My favorite part of the project was working out how to weight the selection process based on the rating. This feature was inspired by the checkbox in iTunes titled "Play higher-rated songs more often". By implementing a weighted rating system, the ratings suddenly have a value to the user of more than just an indicator which hopefully will get the user to use the rating system.

The weighted rating works by doing the following:
* Create a list of all the games available for play (which is based on the number of players and the game types selected). 
* Assign each game a weight based on their rating: 
  * 1-4 stars get assigned their actual star rating.
  * 0 stars gets a rating of 5 to give it more value than played games so the unplayed games will get played.
  * 5 star games get a weight of 6 to make them the most commonly played games. 
* Sum all the weighted values.
* Select a random number where the sum is the maximum allowed value.
* Loop through the list of available games
  * Subtract the weighted rating value of the game from the weighted sum
  * if the sum <= 0 then select game and exit loop

The weighted rating code begins on line 1477 of GameSelector.java