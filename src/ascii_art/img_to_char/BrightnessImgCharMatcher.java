package ascii_art.img_to_char;

import image.Image;
import java.awt.*;
import java.util.*;

/**
 * The role of this class is to match a two-dimensional array of characters that represents an ASCII
 * image to a two-dimensional array of pixels that represents a normal image
 */
public class BrightnessImgCharMatcher {
    private static final int NUM_OF_PIXELS_TO_RENDER_CHAR = 16;
    private static final double MAX_RGB_VALUE = 255;
    private static final Double RED_PRODUCT = 0.2126;
    private static final double GREEN_PRODUCT = 0.7152;
    private static final double BLUE_PRODUCT = 0.0722;
    private final Image img;
    private final String font;
    private final static HashMap<Character, Double> savedCharsBrightnesses = new HashMap<>();
    private final static HashMap<Integer, ArrayList<ArrayList<Double>>> savedSubImagesBrightnesses =
            new HashMap<>();
    private static int lastImageIdentifier = 1;

    /**
     *
     * @param img -> the orig img
     * @param font -> a font of the chars that will be in the ASCII image
     */
    public BrightnessImgCharMatcher(Image img, String font) {
        this.img = img;
        this.font = font;
        if (img.getImageIdentifier() > lastImageIdentifier) {
            savedSubImagesBrightnesses.clear();
        }
        lastImageIdentifier = img.getImageIdentifier();
    }

    /**
     *
     * @param numCharsInRow -> Given a row in the two-dimensional array that represents an image,
     *                     this number is the number of characters that will replace the row of pixels in the
     *                     normal image. This number is blocked from above by the number of pixels in a row
     *                     in the normal image.
     * @param charSet -> The array of characters from which we will build the ASCII image
     * @return A two-dimensional array of characters whose brightness corresponds to the sub-images in
               the original image. A suitable printing of this array will produce an ASCII image identical
               to the original image
     */
    public char[][] chooseChars(int numCharsInRow, Character[] charSet) {

        if (charSet.length > 0){
            boolean subImagesAlreadyComputed = true;
            Integer keyToAddOrFind = numCharsInRow; //a key to search in the hash map in order to find out if
                                           //the current sub images brightnesses already computed in the past

            ArrayList<ArrayList<Double>> matchingSubImagesBrightnesses =
                    savedSubImagesBrightnesses.get(keyToAddOrFind);
            ArrayList<ArrayList<Double>> newSubImagesBrightnesses = null;
            if (matchingSubImagesBrightnesses == null) { // if the current sub images brightnesses have not
                                                                                // computed in the past
                subImagesAlreadyComputed = false;
                newSubImagesBrightnesses = new ArrayList<>(); // then we need to save the current ones
            }

            return getAsciiImage(numCharsInRow, charSet, subImagesAlreadyComputed, keyToAddOrFind,
                    matchingSubImagesBrightnesses, newSubImagesBrightnesses);
        }
        return new char[0][];
    }

    /**
     * A private function that calculates the brightness values of the sub-images and characters,
     * performs a correlation and returns a two-dimensional array of characters that matches the brightness
     * of the sub-images in the original image
     */
    private char[][] getAsciiImage(int numCharsInRow, Character[] charSet,
                                   boolean subImagesAlreadyComputed, Integer keyToAddOrFind,
                                   ArrayList<ArrayList<Double>> matchingSubImagesBrightnesses,
                                   ArrayList<ArrayList<Double>> newSubImagesBrightnesses) {

        int imgHeight = img.getHeight();
        int imgWidth = img.getWidth();
        int subImageSize = imgWidth / numCharsInRow;
        int numCharsInCol = imgHeight / subImageSize;
        TreeMap<Double, Character> charsBrightnesses = calculateAllCharsBrightnesses(charSet);
        Color[][][] subImages = img.convertToSubImages(subImageSize, imgHeight, imgWidth,img.getPixelArray());
        if (subImagesAlreadyComputed) { // then build the ascii image from brightnesses that already computed
                                                                                               // in the past
            return buildFromSavedSubImages(numCharsInCol, numCharsInRow,
                    charsBrightnesses, matchingSubImagesBrightnesses);
        }

        return buildFromNewSubImages(numCharsInCol, numCharsInRow, subImages, newSubImagesBrightnesses,
                subImageSize, charsBrightnesses, keyToAddOrFind);
    }

    /**
     *  A private function that builds the array of characters through the brightnesses of
     *  previously calculated sub images
     */
    private char[][] buildFromSavedSubImages(int numCharsInCol, int numCharsInRow,
                                     TreeMap<Double, Character> charsBrightnesses,
                                     ArrayList<ArrayList<Double>> matchingSubImagesBrightnesses) {
        double currImageBrightness;
        Map.Entry<Double, Character> closestFromAbove;
        Map.Entry<Double, Character> closestFromBottom;
        char[][] asciiImage = new char[numCharsInCol][numCharsInRow];
        for (int i = 0; i < numCharsInCol; i++) {
            for (int j = 0; j < numCharsInRow; j++) {
                currImageBrightness = matchingSubImagesBrightnesses.get(i).get(j); // get the saved value
                closestFromAbove = charsBrightnesses.ceilingEntry(currImageBrightness);
                closestFromBottom = charsBrightnesses.floorEntry(currImageBrightness);
                if (Math.abs(currImageBrightness - closestFromAbove.getKey()) <=
                        Math.abs(currImageBrightness - closestFromBottom.getKey())) {
                    asciiImage[i][j] = closestFromAbove.getValue();
                } else {
                    asciiImage[i][j] = closestFromBottom.getValue();
                }
            }
        }
        return asciiImage;
    }

    /**
     * A private function that builds the array of characters that will be returned (which represents the
     * ASCII image) by a new calculation of brightnesses of sub-images (since they were not calculated
     * before)  and also saves the newly calculated brightnesses in the hash table.
     */
    private char[][] buildFromNewSubImages(int numCharsInCol, int numCharsInRow, Color[][][] subImages,
                                   ArrayList<ArrayList<Double>> newSubImagesBrightnesses, int subImageSize,
                                   TreeMap<Double, Character> charsBrightnesses, Integer keyToAddOrFind) {
        double currImageBrightness;
        Map.Entry<Double, Character> closestFromAbove;
        Map.Entry<Double, Character> closestFromBottom;
        char[][] asciiImage = new char[numCharsInCol][numCharsInRow];
        for (int i = 0; i < numCharsInCol; i++) {
            ArrayList<Double> innerList = new ArrayList<>();
            newSubImagesBrightnesses.add(innerList);
            for (int j = 0; j < numCharsInRow; j++) {
                currImageBrightness = calculateSubImageBrightness(subImages[i][j], subImageSize);
                newSubImagesBrightnesses.get(i).add(currImageBrightness); // save the subImage's brightness
                closestFromAbove = charsBrightnesses.ceilingEntry(currImageBrightness);
                closestFromBottom = charsBrightnesses.floorEntry(currImageBrightness);
                if(closestFromAbove != null && closestFromBottom != null) { //means there are at least 2 chars
                    if (Math.abs(currImageBrightness - closestFromAbove.getKey()) <=
                            Math.abs(currImageBrightness - closestFromBottom.getKey())) {
                        asciiImage[i][j] = closestFromAbove.getValue();
                    } else {
                        asciiImage[i][j] = closestFromBottom.getValue();
                    }
                }else { // in this case it means there aren't 2 different values of char brightnesses
                    asciiImage[i][j] = charsBrightnesses.firstEntry().getValue();
                }
            }
        }
        savedSubImagesBrightnesses.put(keyToAddOrFind, newSubImagesBrightnesses);
        return asciiImage;
    }

    /**
     * A private function that calculates the brightness of the characters if these characters have not
     * been calculated before
     */
    private TreeMap<Double, Character> calculateAllCharsBrightnesses(Character[] charSet) {
        int charSetLength = charSet.length;
        ArrayList<Double> tmpCharsBrightnesses = new ArrayList<>();
        for (int i = 0; i < charSetLength; i++) {
            Double charBrightnessAlreadyComputed = savedCharsBrightnesses.get(charSet[i]);
            if (charBrightnessAlreadyComputed != null) { // means the char already computed in the past
                tmpCharsBrightnesses.add(charBrightnessAlreadyComputed);
            } else { // compute the brightness of the char then save it in the hash map
                tmpCharsBrightnesses.add(calculateCharBrightness(charSet[i], font));
                savedCharsBrightnesses.put(charSet[i], tmpCharsBrightnesses.get(i));
            }
        }
        double maxBrightness = Collections.max(tmpCharsBrightnesses);
        double minBrightness = Collections.min(tmpCharsBrightnesses);
        TreeMap<Double, Character> charsBrightnesses = new TreeMap<>();
        for (int i = 0; i < charSetLength; i++) { // normalize the brightnesses
            if(maxBrightness - minBrightness != 0) {
                charsBrightnesses.put((tmpCharsBrightnesses.get(i) - minBrightness) /
                        (maxBrightness - minBrightness), charSet[i]);
            }else {
                charsBrightnesses.put(tmpCharsBrightnesses.get(i), charSet[i]);
            }
        }
        return charsBrightnesses;
    }

    /**
     *
     * @param c -> char to compute its brightness
     * @param fontName -> specific font for the char
     * @return the computed brightness of the char
     */
    private double calculateCharBrightness(char c, String fontName) {
        boolean[][] charImage = CharRenderer.getImg(c, NUM_OF_PIXELS_TO_RENDER_CHAR, fontName);
        double sumWhitePixels = 0;
        for (int i = 0; i < NUM_OF_PIXELS_TO_RENDER_CHAR; i++) {
            for (int j = 0; j < NUM_OF_PIXELS_TO_RENDER_CHAR; j++) {
                if (charImage[i][j]) {
                    sumWhitePixels++;
                }
            }
        }
        return sumWhitePixels / (NUM_OF_PIXELS_TO_RENDER_CHAR * NUM_OF_PIXELS_TO_RENDER_CHAR);
    }

    /**
     *
     * @param subImage -> sub image to compute its brightness by computing brightness for each pixel
     * @param num_of_pixels -> the number of pixels in the subImage
     * @return the computed brightness of the subImage
     */
    private double calculateSubImageBrightness(Color[] subImage, int num_of_pixels) {
        double sumGreyPixels = 0;
        for (int i = 0; i < num_of_pixels * num_of_pixels; i++) {
            sumGreyPixels += subImage[i].getRed() * RED_PRODUCT + subImage[i].getGreen() *
                    GREEN_PRODUCT + subImage[i].getBlue() * BLUE_PRODUCT;
        }
        sumGreyPixels /= (num_of_pixels * num_of_pixels);
        return sumGreyPixels / MAX_RGB_VALUE;
    }


}
