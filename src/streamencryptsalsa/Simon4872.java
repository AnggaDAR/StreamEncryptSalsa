/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package streamencryptsalsa;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author CVGS
 */
public class Simon4872 {

    static int rounds, wordSize, keyWords, blockSize, keySize, c;
    static int[] key, key2;

    public Simon4872(String k) {
        /*Inisialisasi parameter untuk metode simon 48/72*/
        rounds = 36;//jumlah iterasi fungsi simon dieksekusi
        wordSize = 24;//ukuran kata
        keyWords = 3;//jumlah kata kunci
        blockSize = 2 * wordSize;//jumlah block
        keySize = keyWords * wordSize;//ukuran kunci
        c = 0xFFFFFC;//konstanta 2^n-4
        key = keyExpansion(k);//ekspansi kunci
    }

//    public static void main(String[] args) {
//        String k = "1211100a0908020100";
//        Simon4872 simon = new Simon4872(k);
////        for (int i = 0; i < key.length; i++) {
////            System.out.println(Integer.toHexString(key[i]));
////        }
//    }
    public void encrypt48_72(String img, String encImg) throws IOException {
        BufferedImage image = ImageIO.read(new File(img));//pembacaan gambar yang akan dienkripsi
        FileOutputStream fo;
        BufferedOutputStream bo;
        int width, height, d = 0, x, y, tmp;
        int[] pixelRGB, encPixelRGB;

        width = image.getWidth();
        height = image.getHeight();
        pixelRGB = new int[width * height];
        encPixelRGB = new int[width * height];
        /*Pembacaan RGB per pixel pada gambar*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixelRGB[d] = image.getRGB(j, i) & 0xFFFFFF;
                d++;
            }
        }
        /*Fungsi enkripsi simon 48/72 pada nilai RGB*/
        for (int i = 0; i < pixelRGB.length / 2; i++) {
            int index = i * 2;
            x = pixelRGB[index];
            y = pixelRGB[index + 1];
            for (int j = 0; j < rounds; j++) {
                tmp = x;
                x = (y ^ (rotateLeft(x, 1, 24) & rotateLeft(x, 8, 24)) ^ rotateLeft(x, 2, 24) ^ key[j]) & 0xFFFFFF;//x = y XOR ((S^1)x AND (S^8)x) XOR (S^2)x XOR k[i]
                y = tmp;//y = x
            }
            encPixelRGB[index] = x;
            encPixelRGB[index + 1] = y;
        }

        d = 0;
        /*Penyimpanan nilai RGB yang telah dienkripsi*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color newColor = new Color(encPixelRGB[d]);
                image.setRGB(j, i, newColor.getRGB());
                d++;
            }
        }
        fo = new FileOutputStream(encImg);
        bo = new BufferedOutputStream(fo);
        ImageIO.write(image, "png", bo);//penyimpanan gambar hasil enkripsi
        bo.close();
    }

    public void decrypt48_72(String encImg, String decImg) throws IOException {
        BufferedImage image = ImageIO.read(new File(encImg));//pembacaan gambar yang akan didekripsi
        int width, height, d = 0;
        int x, y, tmp;
        width = image.getWidth();
        height = image.getHeight();
        int[] encPixelRGB = new int[width * height];
        int[] decPixelRGB = new int[width * height];
        /*Pembacaan RGB per pixel pada gambar*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                encPixelRGB[d] = image.getRGB(j, i) & 0xFFFFFF;
                d++;
            }
        }
        /*Fungsi dekripsi simon 48/72 pada nilai RGB*/
        for (int i = 0; i < encPixelRGB.length / 2; i++) {
            int index = i * 2;
            x = encPixelRGB[index];
            y = encPixelRGB[index + 1];
            for (int j = rounds - 1; j >= 0; j--) {
                tmp = y;
                y = (x ^ (rotateLeft(y, 1, 24) & rotateLeft(y, 8, 24)) ^ rotateLeft(y, 2, 24) ^ key[j]) & 0xFFFFFF;//y = x XOR ((S^1)y AND (S^8)y) XOR (S^2)y XOR k[i]
                x = tmp;//x = y
            }
            decPixelRGB[index] = x;
            decPixelRGB[index + 1] = y;
        }

        d = 0;
        /*Penyimpanan nilai RGB yang telah didekripsi*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color newColor = new Color(decPixelRGB[d]);
                image.setRGB(j, i, newColor.getRGB());
                d++;
            }
        }
        /*-------------------------------------------*/
        FileOutputStream fo = new FileOutputStream(decImg);
        BufferedOutputStream bo = new BufferedOutputStream(fo);
        ImageIO.write(image, "png", bo);//penyimpanan gambar hasil dekripsi
        bo.close();
    }

    public int[] keyExpansion(String key) {
        int[] k = new int[rounds];
        int[] z0 = {1, 1, 1, 1, 1, 0, 1, 0, 0, 0,
            1, 0, 0, 1, 0, 1, 0, 1, 1, 0,
            0, 0, 0, 1, 1, 1, 0, 0, 1, 1,
            0, 1, 1, 1, 1, 1, 0, 1, 0, 0,
            0, 1, 0, 0, 1, 0, 1, 0, 1, 1,
            0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0};//konstanta z0 untuk metode simon 48/72
        /*Inisialisasi k[keyWords-1]..k[0]*/
        for (int i = 0; i < keyWords; i++) {
            int index = 12 - (i * 6);
            k[i] = Integer.parseInt(key.substring(index, index + 6), 16) & 0xFFFFFF;
//            System.out.println(key.substring(index, index + 6) + "|" + Integer.toBinaryString(k[i]));
        }
        /*Ekspansi Kunci*/
        for (int i = keyWords; i < rounds; i++) {
            int tmp = rotateRight(k[i - 1], 3, 24);
//            System.out.println(Integer.toBinaryString(k[i - 1]) + "|" + Integer.toBinaryString(tmp));
            if (keyWords == 4) {
                tmp ^= k[i - 3];
            }
//            System.out.println(Integer.toBinaryString(tmp) + "|" + Integer.toBinaryString(rotateRight(tmp, 1, 24)));
            tmp = tmp ^ rotateRight(tmp, 1, 24);
//            System.out.println(Integer.toBinaryString(tmp));
//            System.out.println("");
            k[i] = (tmp ^ k[i - keyWords] ^ z0[(i - keyWords) % 62] ^ c) & 0xFFFFFF;
//            System.out.println("tmp = " + Integer.toBinaryString(tmp));
//            System.out.println("k[" + (i - keyWords) + "] = " + Integer.toBinaryString(k[i - keyWords]));
//            System.out.println("z0[" + ((i - keyWords) % 62) + "] = " + Integer.toBinaryString(z0[(i - keyWords) % 62]));
//            System.out.println("c = " + Integer.toBinaryString(c));
//            System.out.println("k[" + i + "] = " + Integer.toBinaryString(k[i]));
//            System.out.println("");
        }
        return k;
    }

    public int rotateRight(int n, int s, int bits) {
        return ((n >>> s) | (n << (bits - s)));//Rotasi nilai bit RGB per pixel ke kanan
    }

    public int rotateLeft(int n, int s, int bits) {
        return ((n << s) | (n >>> (bits - s)));//Rotasi nilai bit RGB per pixel ke kiri
    }

}
