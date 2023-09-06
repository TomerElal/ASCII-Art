package image;

import java.awt.*;
import java.io.IOException;

/**
 * Facade for the image module and an interface representing an image.
 *
 * @author Dan Nirel
 */
public interface Image {
    Color getPixel(int x, int y);

    int getWidth();

    int getHeight();




    /**
     * Open an image from file. Each dimensions of the returned image is guaranteed
     * to be a power of 2, but the dimensions may be different.
     *
     * @param filename a path to an image file on disk
     * @return an object implementing Image if the operation was successful,
     * null otherwise
     */
    static Image fromFile(String filename) {
        try {
            return new FileImage(filename);
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Allows iterating the pixels' colors by order (first row, second row and so on).
     *
     * @return an Iterable<Color> that can be traversed with a foreach loop
     */
    default Iterable<Color> pixels() {
        return new ImageIterableProperty<>(
                this, this::getPixel);
    }

    /**
     *
     * @param size -> The dimension of the square matrix which represents a sub image
     * @param height -> height of the orig image
     * @param width -> width of the orig image
     * @param pixelArray -> a pixel array of the orig image
     * @return -> a pixel array of the new image which represents now with sub images
     */
    default Color[][][] convertToSubImages(int size, int height, int width, Color[][] pixelArray) {
        Color[][][] subImages = new Color[(height / size)][(width / size)][size * size];
        for (int subImagesRow = 0; subImagesRow < height / size; subImagesRow++) {
            for (int subImagesCol = 0; subImagesCol < width / size; subImagesCol++) {
                createSubImage(size, subImages, subImagesRow, subImagesCol, pixelArray);
            }
        }
        return subImages;
    }

    /**
     * Helper function that creates single sub image with appropriate coordinates
     * @param size -> The dimension of the square matrix which represents a sub image
     * @param subImages -> a pixel array of the new image which represents now with sub images
     * @param subImagesRow -> The row in the returned image where we will insert the current sub-image
     * @param subImagesCol -> The column in the returned image where we will insert the current sub-image
     * @param pixelArray -> a pixel array of the orig image
     */
    private void createSubImage(int size, Color[][][] subImages, int subImagesRow,
                                int subImagesCol, Color[][] pixelArray) {
        int counter = -1;
        for (int i = 0; i < size * size; i++) {
            if (i % size == 0) {
                counter++;
            }
            subImages[subImagesRow][subImagesCol][i] =
                    pixelArray[((subImagesRow * size) + counter)][(subImagesCol * size) + (i % size)];
        }
    }


    default int getImageIdentifier() {
        return 0;
    }

    default Color[][] getPixelArray() {
        return null;
    }
}
