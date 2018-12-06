package hangman;

import java.io.File;
import java.io.IOException;

import hangman.EvilHangmanGame;
//upper case lower case same thing, enter invalid input
public class Main {
    public static void main(String[] args)throws IOException, IEvilHangmanGame.GuessAlreadyMadeException{
        String pathName = args[0];
        String wordLengthStr = args[1];
        int wordLength = Integer.parseInt(wordLengthStr);
        String guessesStr = args[2];
        int guesses = Integer.parseInt(guessesStr);
        File file = new File(pathName);
        EvilHangmanGame game = new EvilHangmanGame();
        game.startGame(file, wordLength);
        game.guessesAllowed(guesses);
        game.playGame();
        //start game and make guess

    }
}
