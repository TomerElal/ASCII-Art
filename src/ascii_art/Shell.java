package ascii_art;

import ascii_art.img_to_char.BrightnessImgCharMatcher;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import java.util.HashSet;
import java.util.Scanner;

/**
 * This class implements the entire user interface when running the program
 * that converts a normal image to an ASCII image.
 */
public class Shell {
    private static final int MIN_PIXELS_PER_CHAR = 2;
    private static final int INITIAL_CHARS_IN_ROW = 64;
    private static final char SPACE_CHAR = ' ';
    private static final char LAST_CHAR_IN_ASCII = '~';
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String ALL_COMMAND = "all";
    private static final String SPACE_COMMAND = "space";
    private static final char HYPHEN_CHAR = '-';
    private static final String FORMAT_ERROR_MESSAGE = "Did not %s due to incorrect format%n";
    private static final String COMMAND_ERROR_MESSAGE = "Did not executed due to incorrect command";
    private static final String BOUNDARIES_ERROR_MESSAGE = "Did not change due to exceeding boundaries";
    private static final String WIDTH_SET_TO = "Width set to ";
    private static final String CHARS = "chars";
    private static final String RESOLUTION_UP = "res up";
    private static final String RESOLUTION_DOWN = "res down";
    private static final String CONSOLE = "console";
    private static final String RENDER = "render";
    private static final String FONT = "Courier New";
    private static final String EMPTY_STRING = "";
    private static final String OUT_FILE = "out.html";
    private static final String EXIT_MESSAGE = "exit";
    private static final String MESSAGE_FOR_USER = ">>> ";
    private static final String SPACE_STRING = " ";
    private static final int BEGIN_CHAR_FOR_INITIATE = 48;
    private static final int END_CHAR_FOR_INITIATE = 58;
    private static final int LEGAL_BEGIN_CHAR = 33;
    private static final int LEGAL_END_CHAR = 126;
    private static final String NO_CHARS_ERROR_MESSAGE = "Can not render without any chars";
    private final int minCharsInRow;
    private final int maxCharsInRow;
    private int charsInRow;
    private final Image img;
    private final HashSet<Character> chars;
    AsciiOutput asciiOutput;

    /**
     * constructor
     * @param img -> An image to convert into an ASCII image
     * The constructor also initializes a basic list of chars which will be presented in the ascii image as
       pixels (in case the user won't remove them).
     */
    public Shell(Image img) {
        this.img = img;
        chars = new HashSet<>();
        initiateChars();
        minCharsInRow = Math.max(1, img.getWidth()/img.getHeight());
        maxCharsInRow = img.getWidth() / MIN_PIXELS_PER_CHAR;
        charsInRow = Math.max(Math.min(INITIAL_CHARS_IN_ROW, maxCharsInRow), minCharsInRow);
        asciiOutput = new HtmlAsciiOutput(OUT_FILE, FONT);
    }

    /**
     * This function initializes a basic list of characters - the characters zero through nine
     */
    private void initiateChars() {
        for (int i = BEGIN_CHAR_FOR_INITIATE; i < END_CHAR_FOR_INITIATE; i++) {
            chars.add((char) (i));
        }
    }

    /**
     * This function executes all commands from the user. The commands are running the program, which means
     * receiving the output of an ASCII image, adding or downloading characters that will appear in the ASCII
     * image, reducing or increasing the resolution of the image and printing the current characters
     * in the program.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String userInput = EMPTY_STRING;
        while (!userInput.equals(EXIT_MESSAGE)){
            System.out.print(MESSAGE_FOR_USER);
            userInput = scanner.nextLine();
            handleUserInput(userInput);
        }
    }

    /**
     *This function handles the input that the user entered and executes the appropriate function.
     * @param userInput -> The input that the user entered
     */
    private void handleUserInput(String userInput) {
        switch (userInput) {
            case CHARS:
                printChars();
                break;
            case RESOLUTION_UP:
                resUp();
                break;
            case RESOLUTION_DOWN:
                resDown();
                break;
            case CONSOLE:
                asciiOutput = new ConsoleAsciiOutput();
                break;
            case RENDER:
                renderAsciiImage();
                break;
            default:
                handleAddRemoveCommand(userInput);
                break;
        }
}

    /**
     * This function outputs the final output which is an ASCII image when the output will be printed in one
     * of two options according to the user's choice - either as an HTML file or as printing to the console
     */
    private void renderAsciiImage() {
        if(!chars.isEmpty()){
            BrightnessImgCharMatcher imgCharMatcher = new BrightnessImgCharMatcher(img, FONT);
            Character[] arrayChars = new Character[chars.size()];
            asciiOutput.output(imgCharMatcher.chooseChars(charsInRow, chars.toArray(arrayChars)));
        }else {
            System.out.println(NO_CHARS_ERROR_MESSAGE);
        }
    }

    /**
     * Lowers the ASCII image resolution
     */
    private void resDown() {
        if (charsInRow / 2 >= minCharsInRow) {
            charsInRow /= 2;
            System.out.println(WIDTH_SET_TO + charsInRow);
        } else {
            System.out.println(BOUNDARIES_ERROR_MESSAGE);
        }
    }

    /**
     * Raises the ASCII image resolution
     */
    private void resUp() {
        if (charsInRow * 2 <= maxCharsInRow) {
            charsInRow *= 2;
            System.out.println(WIDTH_SET_TO + charsInRow);
        } else {
            System.out.println(BOUNDARIES_ERROR_MESSAGE);
        }
    }

    /**
     *This function decides whether it is a command to add or remove characters
     * and calls the function to execute the command respectively
     * @param userInput -> The input that the user entered
     */
    private void handleAddRemoveCommand(String userInput) {
        if (userInput.startsWith(ADD)){
            executeAddRemoveCommand(userInput.substring(4), false);
        }else if(userInput.startsWith(REMOVE)){
            executeAddRemoveCommand(userInput.substring(7), true);
        }else if(!userInput.equals(EXIT_MESSAGE)){
            System.out.println(COMMAND_ERROR_MESSAGE);
        }
    }

    /**
     * This is the function that executes the add/remove character command
     * @param userInput -> The input that the user entered
     * @param addOrRemove -> Indicates whether it's a remove/add command
     */
    private void executeAddRemoveCommand(String userInput, boolean addOrRemove) {
        String commandType = chooseCommandType(addOrRemove);
        if(userInput.equals(ALL_COMMAND)){ // case of insert/remove all chars
            insertOrRemoveCharValues(SPACE_CHAR, LAST_CHAR_IN_ASCII, addOrRemove);
        }else if(userInput.equals(SPACE_COMMAND)){ // case of insert/remove the space char
            insertOrRemoveCharValues(SPACE_CHAR, SPACE_CHAR, addOrRemove);
        }else if(userInput.length() == 3 && userInput.charAt(1) == HYPHEN_CHAR){ //case of "char-char" command
            insertOrRemoveCharacterSequence(userInput, addOrRemove, commandType);
        }else if(userInput.length() == 1){
            char specificChar = userInput.charAt(0);
            insertOrRemoveCharValues(specificChar, specificChar, addOrRemove);
        }else {
            System.out.printf(FORMAT_ERROR_MESSAGE, commandType);
        }
    }

    /**
     * This function adds/removes a sequence of characters according to user input
     * @param userInput -> The input that the user entered
     * @param addOrRemove -> Indicates whether it's a remove/add command
     * @param commandType -> a string which could be "add" or "remove"
     */
    private void insertOrRemoveCharacterSequence(String userInput, boolean addOrRemove, String commandType) {
        char begin = userInput.charAt(0);
        char end = userInput.charAt(2);
        if(validateCharRange(begin) && validateCharRange(end)){
            if(begin > end){
                char tmp = begin;
                begin = end;
                end = tmp;
            }
            insertOrRemoveCharValues(begin, end, addOrRemove);
        }else{
            System.out.printf(FORMAT_ERROR_MESSAGE, commandType);
        }
    }

    /**
     *
     * @param isRemove -> Indicates whether it's a remove/add command
     * @return a string which will be "add" or "remove"
     */
    private static String chooseCommandType(boolean isRemove) {
        String s = ADD;
        if (isRemove) {
            s = REMOVE;
        }
        return s;
    }

    /**
     * Helper function for executing the commands to add/remove characters
     * @param begin -> the ASCII value to begin inserting chars from.
     * @param end -> The ASCII value for which we will stop inserting characters
     * @param removeCommand -> Indicates whether it's a remove/add command
     */
    private void insertOrRemoveCharValues(char begin, char end, boolean removeCommand) {
        for (int i = begin; i <= end; i++) {
            if (removeCommand)
            {
                chars.remove((char) i);
            }else {
                chars.add((char) i);
            }
        }
    }

    /**
     * This function prints the current characters in the program
     */
    private void printChars() {
        for (Character character: chars){
            System.out.print(character + SPACE_STRING);
        }
        System.out.println();
    }

    /**
     *
     * @param charToValidate -> a char to check that it does not exceed the legal character limits
     * @return true if it is a legal char
     */
    private boolean validateCharRange(char charToValidate){
        return charToValidate >= LEGAL_BEGIN_CHAR && charToValidate <= LEGAL_END_CHAR;
    }
}
