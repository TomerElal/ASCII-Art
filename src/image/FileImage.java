package image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.*;
import static java.lang.Math.log;

/**
 * A package-private class of the package image.
 *
 * @author Dan Nirel
 */
class FileImage implements Image {
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static int counter = 0;
    int width;
    int height;

    private Color[][] pixelArray;
    private final int imageIdentifier;

    /**
     * constructor
     * @param filename -> a given file image name
     * @throws IOException in case system couldn't read the file
     */
    public FileImage(String filename) throws IOException {
        java.awt.image.BufferedImage im = ImageIO.read(new File(filename));
        int origWidth = im.getWidth(), origHeight = im.getHeight();
        imageIdentifier = counter++;
        width = (int) pow(2, ceil(log(origWidth) / log(2)));
        height = (int) pow(2, ceil(log(origHeight) / log(2)));
        padImage(im, origWidth, origHeight);
    }

    /**
     * This function creates a padding envelope around the image in order to turn it
     * into an image whose dimensions are powers of 2
     * @param im -> an image to work on
     * @param origWidth of the given image
     * @param origHeight of the given image
     */
    private void padImage(BufferedImage im, int origWidth, int origHeight) {

        pixelArray = new Color[height][width];
        int upperBound = (height - origHeight) / 2;
        int lowerBound = upperBound + origHeight;
        int leftBound = (width - origWidth) / 2;
        int rightBound = leftBound + origWidth;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if ((i >= upperBound && i < lowerBound) && (j >= leftBound && j < rightBound)) {
                    pixelArray[i][j] = new Color(im.getRGB(j - leftBound, i - upperBound));
                } else {
                    pixelArray[i][j] = DEFAULT_COLOR;
                }
            }
        }
    }

    /**
     *
     * @return the width of the picture
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     *
     * @return the height of the picture
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     *
     * @param x a row
     * @param y a column
     * @return specific pixel
     */
    @Override
    public Color getPixel(int x, int y) {
        return pixelArray[x][y];
    }

    /**
     *
     * @return a number that represents an image
     */
    @Override
    public int getImageIdentifier() {
        return imageIdentifier;
    }

    /**
     *
     * @return the pixel array of the image
     */
    @Override
    public Color[][] getPixelArray() {
        return pixelArray;
    }
}
