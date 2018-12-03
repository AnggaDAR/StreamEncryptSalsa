/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streamencryptsalsa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import static streamencryptsalsa.test.rotateLeft;

/**
 *
 * @author CVGS
 */
public class coba {

    public static void main(String[] args) throws IOException {
//        String a = "111101101011001101011101";//dec 16167773
//        String b = "011011011010111000100101";//dec 7188005
//        int tmp;
//        int x = Integer.parseInt(a, 2);
//        int y = Integer.parseInt(b, 2);
//        System.out.println("x|" + String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0') + "|" + String.format("%32s", Integer.toBinaryString(y)).replace(' ', '0'));
//        for (int j = 0; j < 44; j++) {
//            tmp = x;
//            x = y ^ (rotateLeft(x, 1, 24) & rotateLeft(x, 8, 24)) ^ rotateLeft(x, 2, 24);
//            y = tmp;
//            System.out.println(j + "|" + String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0') + "|" + String.format("%32s", Integer.toBinaryString(y)).replace(' ', '0'));
//        }
//        System.out.println("---------------------------------------------------");
//        System.out.println("x|" + String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0') + "|" + String.format("%32s", Integer.toBinaryString(y)).replace(' ', '0'));
//        for (int j = 0; j < 44; j++) {
//            tmp = y;
//            y = x ^ (rotateLeft(y, 1, 24) & rotateLeft(y, 8, 24)) ^ rotateLeft(y, 2, 24);
//            x = tmp;
//            System.out.println(j + "|" + String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0') + "|" + String.format("%32s", Integer.toBinaryString(y)).replace(' ', '0'));
//        }
//        System.out.println(Integer.toBinaryString(255));
//        System.out.println(Integer.toBinaryString(128));
//        ambilWarna();
        String a = "000000";
        String b = "FFFFFF";
        String k1 = "121110";
        String k2 = "0a0908";
        String k3 = "020100";
        long start = System.currentTimeMillis();
        double start2 = start;
        int xa = Integer.parseInt(a, 16);
        int xb = Integer.parseInt(b, 16);
        int xk1 = Integer.parseInt(k1, 16);
        int xk2 = Integer.parseInt(k2, 16);
        int xk3 = Integer.parseInt(k3, 16);
        String binA = Integer.toBinaryString(xa);
        String binB = Integer.toBinaryString(xb);
        String binK1 = Integer.toBinaryString(xk1);
        String binK2 = Integer.toBinaryString(xk2);
        String binK3 = Integer.toBinaryString(xk3);
        System.out.println(xa + "|" + xb);
        System.out.println(binA + "|" + binB);
        System.out.println(xk1 + "|" + xk2 + "|" + xk3);
        System.out.println(binK1 + "|" + binK2 + "|" + binK3);
        Random r = new Random();
        int rand;
        for (int i = 0; i < 10; i++) {
            rand = r.nextInt(16777215);
            System.out.println(rand + " | " + String.format("%06x", rand) + " | " + Integer.toHexString(rand));
        }
        long end = System.currentTimeMillis();
        double end2 = end;
        System.out.println(start + "-" + end);
        System.out.println(start2 + "-" + end2);
        System.out.println(start - end);
        System.out.println(start2 - end2);
        System.out.printf("%d - %d = %d\n%f - %f = %f", start, end, start - end, start2, end2, start2 - end2);
        System.out.println("\n"+Integer.toBinaryString(Integer.parseInt("121110",16)));
    }

    static int rotateLeft(int n, int s, int bits) {
        return ((n << s) | (n >>> (bits - s))) & 0xFFFFFF;
    }

    public int rotateRight(int n, int s, int bits) {
        return ((n >>> s) | (n << (bits - s))) & 0xFFFFFF;
    }

    static void ambilWarna() throws IOException {
        int wordSize = 32, keyWords = 4, rounds = 44;
        BufferedImage image1 = ImageIO.read(new File("server/capture.png"));
        int width, height, d = 0;
        int red, green, blue;
        int x, y, tmp;
        width = image1.getWidth();
        height = image1.getHeight();
        int[] pixelRGB = new int[width * height];
        int[] encPixelRGB = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image1.getRGB(j, i));
                red = c.getRed();
                green = c.getGreen();
                blue = c.getBlue();
                pixelRGB[d] = ((red << 16) | (green << 8) | blue) & 0xFFFFFF;
                d++;
            }
        }
        for (int i = 0; i < pixelRGB.length / 2; i++) {
            int index = i * 2;
            x = pixelRGB[index];
            y = pixelRGB[index + 1];
            for (int j = 0; j < 44; j++) {
                tmp = x;
                x = y ^ (rotateLeft(x, 1, 24) & rotateLeft(x, 8, 24)) ^ rotateLeft(x, 2, 24);
                y = tmp;
            }
            encPixelRGB[index] = x;
            encPixelRGB[index + 1] = y;
        }
        d = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color newColor = new Color(encPixelRGB[d]);
                image1.setRGB(j, i, newColor.getRGB());
                d++;
            }
        }
        ImageIO.write(image1, "png", new File("test/enc.png"));
        BufferedImage image2 = ImageIO.read(new File("client/encrypted.png"));
        width = image2.getWidth();
        height = image2.getHeight();
        int[] encPixelRGB2 = new int[width * height];
        int[] decPixelRGB = new int[width * height];
        d = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image2.getRGB(j, i));
                red = c.getRed();
                green = c.getGreen();
                blue = c.getBlue();
                encPixelRGB2[d] = ((red << 16) | (green << 8) | blue) & 0xFFFFFF;
                d++;
            }
        }
        for (int i = 0; i < encPixelRGB2.length / 2; i++) {
            int index = i * 2;
            x = encPixelRGB2[index];
            y = encPixelRGB2[index + 1];
            for (int j = 0; j < 44; j++) {
                tmp = y;
                y = x ^ (rotateLeft(y, 1, 24) & rotateLeft(y, 8, 24)) ^ rotateLeft(y, 2, 24);
                x = tmp;
            }
            decPixelRGB[index] = x;
            decPixelRGB[index + 1] = y;
        }
        d = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color newColor = new Color(decPixelRGB[d]);
                image2.setRGB(j, i, newColor.getRGB());
                d++;
            }
        }
        ImageIO.write(image2, "png", new File("test/dec.png"));
    }
}
