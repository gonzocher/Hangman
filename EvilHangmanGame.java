package hangman;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class EvilHangmanGame implements IEvilHangmanGame {
    private int gameWordLength;
    private int gameGuesses;
    private Set<String> dictionaryWords = new HashSet<String>();
    private String gamePattern;
    private Set<Character> guesses = new TreeSet<Character>();


    public EvilHangmanGame(){}

    public String defaultGamePattern(){
        char[] patternChars = new char[gameWordLength];
        for(int i = 0; i < gameWordLength; i++){
            patternChars[i] = '-';
        }
        return String.valueOf(patternChars);
    }


        @SuppressWarnings("serial")
        public static class GuessAlreadyMadeException extends Exception {
            public GuessAlreadyMadeException(){
            }
        }

        /**
         * Starts a new game of evil hangman using words from <code>dictionary</code>
         * with length <code>wordLength</code>.
         *	<p>
         *	This method should set up everything required to play the game,
         *	but should not actually play the game. (ie. There should not be
         *	a loop to prompt for input from the user.)
         *
         * @param dictionary Dictionary of words to use for the game
         * @param wordLength Number of characters in the word to guess
         */
        public void startGame(File dictionary, int wordLength){
            try {
                FileInputStream fis = new FileInputStream(dictionary);
                BufferedInputStream bis = new BufferedInputStream(fis);
                try(Scanner scanner = new Scanner(bis)){
                    while(scanner.hasNext()){
                        dictionaryWords.add(scanner.next());
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*
            print dictionary
            for(String s:dictionaryWords){
                System.out.println(s.toString());
            }*/
            gameWordLength = wordLength;
            dictionaryWords = wordLengthSub();
            gamePattern = defaultGamePattern();
        }

        public Set<String> wordLengthSub(){
            Set<String> substrWords = new HashSet<String>();
            for(String s: dictionaryWords){
                char[] sChars = s.toCharArray();
                if(sChars.length == gameWordLength){
                    substrWords.add(s);
                }
            }
            dictionaryWords = substrWords; //modify original dictionary
            return substrWords;
        }

        public void guessesAllowed(int guessesNum){
            gameGuesses = guessesNum;
        }

        //game pattern: --a  biggestKey b--   new pattern b-a
        public void editPattern(String biggestKey){
            char[] newPatternChars = new char[gameWordLength];
            char[] oldPatternChars = gamePattern.toCharArray();
            char[] biggestKeyChars = biggestKey.toCharArray();
            for(int i = 0; i < gameWordLength; i++){
                if (oldPatternChars[i] != '-'){
                    newPatternChars[i] = oldPatternChars[i];
                }
                else if (biggestKeyChars[i] != '-'){
                    newPatternChars[i] = biggestKeyChars[i];
                }
                else{
                    newPatternChars[i] = '-';
                }
            }
            String newPattern = String.valueOf(newPatternChars);
            //System.out.println("newPattern: " + newPattern);
            gamePattern = newPattern; ///must modify not replace the game pattern
        }

        public String makePattern(String word, char guess){
            char[] sChars = word.toCharArray();
            char[] patternChars = new char[sChars.length];
            for(int i = 0; i < sChars.length; i++){
                if (sChars[i] == guess){
                    patternChars[i] = guess;
                }
                else{
                    patternChars[i] = '-';
                }
            }
            String pattern = String.valueOf(patternChars);
            return pattern;
        }

        public int getGuessCount(String key, char guess){
            int count = 0;
            char[] keyChars = key.toCharArray();
            for(int i = 0; i < keyChars.length; i++){
                if(keyChars[i] == guess){
                    count++;
                }
            }
            return count;
        }

        //rightmost-most = 1; next rightmost = 2...
        public int getRightmost(String key, char guess, int most){
            int guessCount = getGuessCount(key,guess);
            int rightPlace = 0;
            char[] keyChars = key.toCharArray();
            int[] rightmostPlaces = new int[guessCount];
            int j = 0;
            for(int i = 0; i < keyChars.length; i++){
                if(keyChars[i] == guess){
                    rightmostPlaces[j] = i;
                    j++;
                }
            }
            rightPlace = rightmostPlaces[guessCount-most];
            return rightPlace;
        }

        //@returns the new substring of the dictionary
        public Set<String> subDictionary(char guess){
            Map<String, Set<String>> patternMap = new HashMap<String, Set<String>>();
            //for every word, make a pattern for it, add it to map
            for(String word:dictionaryWords){
                String pattern = makePattern(word, guess);
                //pattern is in the map-add word to its set
                if(patternMap.containsKey(pattern)){
                    Set<String> patternWords = patternMap.get(pattern);
                    patternWords.add(word);
                    patternMap.replace(pattern, patternWords);
                }
                //pattern is not in the map-add key and set(the word) to map
                else{
                    Set<String> patternWords = new HashSet<String>();
                    patternWords.add(word);
                    patternMap.put(pattern, patternWords);
                }
            }
            Set<String> keys = patternMap.keySet();
            Set<String> biggestGroup = new HashSet<String>();
            String biggestKey = "";
            for(String key: keys){
                //System.out.println("Pattern: " + key);
                Set<String> keyWords = patternMap.get(key);
                //bigger group
                if(keyWords.size() > biggestGroup.size()){
                    //System.out.println("bigger group: " + keyWords.size() + ">" + biggestGroup.size());
                    biggestGroup = keyWords;
                    biggestKey = key; // keep track of group by its key
                }
                else if(keyWords.size() == biggestGroup.size()){
                    int guessInKey = getGuessCount(key, guess);
                    int guessInBigKey = getGuessCount(biggestKey, guess);

                    //group without least guess letters
                    if (guessInKey < guessInBigKey){
                        biggestGroup = keyWords;
                        biggestKey = key;
                    }
                    //group with rightmost letter dmth --a > a-- ; a--a > a-a- > -aa-
                    else if(guessInKey == guessInBigKey){
                        int most = 1;
                        int rightmostKey = getRightmost(key,guess,most);
                        int rightmostBigKey = getRightmost(biggestKey, guess,most);
                        if(rightmostKey > rightmostBigKey){
                            //System.out.println("most1");
                            biggestGroup = keyWords;
                            biggestKey = key;
                        }
                        //next rightmost
                        else if (rightmostKey == rightmostBigKey){
                            while(most < guessInKey){
                                most++;
                                //System.out.println("most: " + most);
                                rightmostKey = getRightmost(key,guess,most);
                                rightmostBigKey = getRightmost(biggestKey, guess,most);
                                if(rightmostKey > rightmostBigKey){
                                    biggestGroup = keyWords;
                                    biggestKey = key;
                                }
                            }
                        }
                    }
                }


               /* System.out.println("Words of new substring: ");
                for(String word:biggestGroup){
                    System.out.println(word);
                }*/
            }
            //System.out.println("Biggest group: " + biggestKey);
            editPattern(biggestKey);
            return biggestGroup;
        }

        /**
         * Make a guess in the current game.
         *
         * @param guess The character being guessed
         *
         * @return The set of strings that satisfy all the guesses made so far
         * in the game, including the guess made in this call. The game could claim
         * that any of these words had been the secret word for the whole game.
         *
         * @throws GuessAlreadyMadeException  If the character <code>guess</code>
         * has already been guessed in this game.
         */
        public Set<String> makeGuess(char guess){
            Set<String> substring = playGameAfterInput(guess);
            /*System.out.println("Words of new substring in make guess: ");
            for(String word:substring){
                System.out.println(word);
            }*/
            return substring;
            //make map key: pattern value: subset with pattern
            //refine the dictionary to the evil subset
        }

        public Set<String> makeGuessIn(char guess)throws hangman.IEvilHangmanGame.GuessAlreadyMadeException{
            if (guesses.contains(guess)){
                throw new hangman.IEvilHangmanGame.GuessAlreadyMadeException();
            }
            else{
                guesses.add(guess);
                Set<String> subDict = subDictionary(guess);
                /*System.out.println("Words of new substring in make guess: ");
                for(String word:subDict){
                    System.out.println(word);
                }*/
                return subDict;
            }
        }

        public boolean wonGame(){
            char[] patternArray = gamePattern.toCharArray();
            for (int i = 0; i < gameWordLength; i++){
                if(patternArray[i] == '-'){
                    return false;
                }
            }
            return true;
        }

        public void playGameBeforeInput(){
            if (gameGuesses > 1){
                System.out.println("You have " + gameGuesses + " guesses left");
            }
            else {
                System.out.println("You have " + gameGuesses + " guess left");
            }
            System.out.print("Used letters:");
            if (guesses != null){
                for (char c : guesses){
                    System.out.print(" " + c);
                }
            }

            System.out.println("");

            System.out.print("Word: ");
            System.out.println(gamePattern);
            String input;
            System.out.print("Enter guess: ");
        }

        public Set<String> playGameAfterInput(char guess){

            //if not part of alphabet
            if ((int)guess < 65 || ((int)guess > 90 && (int)guess < 97) || (int)guess > 122 ){
                //bad input
                System.out.println("Invalid input");
                playGameBeforeInput();
            }
            else {
                //uppercase change to lowercase
                int numberCaseGap = 32;
                if ((int)guess > 64 && ((int)guess < 91)){
                    int guessLower = (int)guess + numberCaseGap;
                    guess = (char)guessLower;
                }
                //gamePattern and dictionaryWords modified
                try{
                    dictionaryWords = makeGuessIn(guess);
                    /*System.out.println("Words of new dictionary: ");
                    for(String word:dictionaryWords){
                        System.out.println(word);
                    }*/
                    //print out new substring
                    if(wonGame()){
                        System.out.println("You win!");
                        System.out.print("Word: ");
                        String secretWord = "";
                        for(String s:dictionaryWords){
                            secretWord = s;
                        }
                        System.out.println(secretWord);
                        gameGuesses = 0;
                    }
                    else{
                        // guesses in the secret word
                        int howMany = getGuessCount(gamePattern, guess);
                        //if guess is in the secret word
                        if(howMany > 0){
                            if (howMany > 1){
                                System.out.println("Yes, there are " + howMany + " " + guess + "'s");
                            }
                            else if (howMany == 1){
                                System.out.println("Yes, there is " + howMany + " " + guess);
                            }
                        }
                      //if not
                        else{
                            System.out.println("Sorry, there are no " + guess + "'s");
                            gameGuesses--;
                        }
                        if (gameGuesses > 0){
                            System.out.println("");
                            playGameBeforeInput();
                        }
                    }
                }catch(hangman.IEvilHangmanGame.GuessAlreadyMadeException e){
                    System.out.println("You already used that letter");
                    playGameBeforeInput(); //put everything in the try
                }
            }
            return dictionaryWords;
        }

        public void playGame(){
            String input;
            playGameBeforeInput();

                try(Scanner in = new Scanner(System.in)){
                    while(gameGuesses > 0 && in.hasNextLine()){
                        input = in.nextLine();
                        if (input.length() != 1){
                            //bad input
                            System.out.println("Invalid input");
                            playGameBeforeInput();
                        }
                        else{
                            char guess = input.charAt(0);
                            playGameAfterInput(guess);
                        }

                    }
                }
            //end game
            if (gameGuesses == 0 && !wonGame()){
                System.out.println("You lose!");
                System.out.print("The word was: ");
                Iterator<String> s = dictionaryWords.iterator();
                String secretWord = s.next();

                System.out.print(secretWord);
            }
        }

}

