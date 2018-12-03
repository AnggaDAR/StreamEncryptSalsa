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
public class SalsaTest {

    int rounds, blockNumber;
    int[] keyStream;
    String constants;

    public SalsaTest(String k, String iv, int msgLength) {
        /*Inisialisasi parameter untuk metode salsa20/20*/
        rounds = 20;//jumlah iterasi fungsi simon dieksekusi
        constants = "657870616e642033322d62797465206b";//expand 32-byte key
        blockNumber = msgLength / 64;//jumlah block berukuran 64 byte sesuai ukuran pesan
        keyStream = salsaCore(k, iv, msgLength);//pembangkitan keystream

    }

    public static void main(String[] args) {
//        String k = "0F62B5085BAE0154A7FA4DA0F34699EC3F92E5388BDE3184D72A7DD02376C91C";
////        String k = "0F62B5085BAE0154A7FA4DA0F34699EC";
//        String iv = "288FF65DC42B92F9";
//        String k = "c1b5d7cf0d1b61ec7a22a53b622d7a83832520a9382f95b52130f48dfec31cd9";
        String k = "8000000000000000000000000000000080000000000000000000000000000000";
//        String key = "c1b5d7cf0d1b61ec7a22a53b622d7a83";
//        String iv = "61066d4e77b9d215";
        String iv = "0000000000000000";

//        int msgLen = 176 * 144 * 3;
        int msgLen = 64;
        Salsa20 salsa = new Salsa20(k, iv, msgLen);
//        int[] l = salsa.salsaCore(k, iv, msgLen);
        int[] l = salsa.keyStream;
//        for (int i = 0; i < key.length; i++) {
//            System.out.println(Integer.toHexString(key[i]));
//        }
//        for (int i = 0; i < l.length; i++) {
//            if (i % 4 == 0) {
//                System.out.println("");
//            }
//            System.out.print(l[i] + " ");
//        }
//        System.out.println("");
        for (int i = 0; i < l.length; i++) {
            if (i % 4 == 0) {
                System.out.print(" ");
            }
            if (i % 16 == 0) {
                System.out.println("");
            }
            if (i % 64 == 0) {
                System.out.println(i / 64);
            }
            System.out.print(String.format("%02x", l[i]));
        }
        System.out.println("");
    }

    public int[] hexStringToIntArray(String s) {
        int[] b = new int[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            b[i] = Integer.parseInt(s.substring(index, index + 2), 16);
//            int v = Integer.parseInt(s.substring(index, index + 2), 16);
//            byte v = Byte.parseByte(s.substring(index, index + 2), 16);
        }
        return b;
    }

    public int loadLittleEndian(int[] x, int offset) {
        return ((x[offset]) & 0xff) | (((x[offset + 1]) & 0xff) << 8) | (((x[offset + 2]) & 0xff) << 16) | (((x[offset + 3]) & 0xff) << 24);
    }

    public int[] storeLittleEndian(int u) {
        int[] x = new int[4];
        x[0] = u & 0xff;

        u >>>= 8;
        x[1] = u & 0xff;

        u >>>= 8;
        x[2] = u & 0xff;

        u >>>= 8;
        x[3] = u & 0xff;
//        return (((x[offset]) & 0xff)<< 24) | (((x[offset + 1]) & 0xff) << 16) | (((x[offset + 2]) & 0xff) << 8) | ((x[offset + 3]) & 0xff) ;
//        return (x[offset] & 0xff) | (x[offset + 1] & 0xff) | (x[offset + 2] & 0xff) | (x[offset + 3] & 0xff);
        return x;
    }

    public int[] salsaCore(String key, String iv, int messageLength) {
//        int[] k;
        int[] ks, x = new int[16], in = new int[16];
        String blockCounter;
//        if (k.length() == 16) {
//            key = k + k;
//        } else if (k.length() == 32) {
//            key = k;
//        }
        String[] word = new String[16];
//        String ks = "";
        ks = new int[messageLength];
        word[0] = constants.substring(0, 8);
        word[1] = key.substring(0, 8);
        word[2] = key.substring(8, 16);
        word[3] = key.substring(16, 24);
        word[4] = key.substring(24, 32);
        word[5] = constants.substring(8, 16);
        word[6] = iv.substring(0, 8);
        word[7] = iv.substring(8, 16);
        word[8] = "00000000";
        word[9] = "00000000";
        word[10] = constants.substring(16, 24);
        word[11] = key.substring(32, 40);
        word[12] = key.substring(40, 48);
        word[13] = key.substring(48, 56);
        word[14] = key.substring(56, 64);
        word[15] = constants.substring(24, 32);

        for (int i = 0; i < blockNumber; i++) {
            blockCounter = String.format("%016x", i);
            word[8] = blockCounter.substring(0, 8);
            word[9] = blockCounter.substring(8, 16);
            int index = i * 64;
//            printWord("Initial State in Big Endian", word);

            for (int j = 0; j < word.length; j++) {
                in[j] = x[j] = loadLittleEndian(hexStringToIntArray(word[j]), 0);
            }
//            printWord("Initial State in Little Endian", x);

            for (int j = 0; j < rounds; j += 2) {
                //  Odd Rounds
                //  quarterRound(x[0], x[4], x[8], x[12]);	// column 1
//                System.out.println("Rounds " + j);
                x[4] ^= rotateLeft(x[0] + x[12], 7);
//                printWord("x[4] ^= rotateLeft(x[0] + x[12], 7)", x);

                x[8] ^= rotateLeft(x[4] + x[0], 9);
//                printWord("x[8] ^= rotateLeft(x[4] + x[0], 9)", x);

                x[12] ^= rotateLeft(x[8] + x[4], 13);
//                printWord("x[12] ^= rotateLeft(x[8] + x[4], 13)", x);

                x[0] ^= rotateLeft(x[12] + x[8], 18);
//                printWord("x[0] ^= rotateLeft(x[12] + x[8], 18)", x);

                //  quarterRound(x[5], x[9], x[13], x[1]);	// column 2
                x[9] ^= rotateLeft(x[5] + x[1], 7);
//                printWord("x[9] ^= rotateLeft(x[5] + x[1], 7)", x);

                x[13] ^= rotateLeft(x[9] + x[5], 9);
//                printWord("x[13] ^= rotateLeft(x[9] + x[5], 9)", x);

                x[1] ^= rotateLeft(x[13] + x[9], 13);
//                printWord("x[1] ^= rotateLeft(x[13] + x[9], 13)", x);

                x[5] ^= rotateLeft(x[1] + x[13], 18);
//                printWord("x[5] ^= rotateLeft(x[1] + x[13], 18)", x);

                //  quarterRound(x[10], x[14], x[2], x[6]);	// column 3
                x[14] ^= rotateLeft(x[10] + x[6], 7);
//                printWord("x[14] ^= rotateLeft(x[10] + x[6], 7)", x);

                x[2] ^= rotateLeft(x[14] + x[10], 9);
//                printWord("x[2] ^= rotateLeft(x[14] + x[10], 9)", x);

                x[6] ^= rotateLeft(x[2] + x[14], 13);
//                printWord("x[6] ^= rotateLeft(x[2] + x[14], 13)", x);

                x[10] ^= rotateLeft(x[6] + x[2], 18);
//                printWord("x[10] ^= rotateLeft(x[6] + x[2], 18)", x);

                //  quarterRound(x[15], x[3], x[7], x[11]);	// column 4
                x[3] ^= rotateLeft(x[15] + x[11], 7);
//                printWord("x[3] ^= rotateLeft(x[15] + x[11], 7)", x);

                x[7] ^= rotateLeft(x[3] + x[15], 9);
//                printWord("x[7] ^= rotateLeft(x[3] + x[15], 9)", x);

                x[11] ^= rotateLeft(x[7] + x[3], 13);
//                printWord("x[11] ^= rotateLeft(x[7] + x[3], 13)", x);

                x[15] ^= rotateLeft(x[11] + x[7], 18);
//                printWord("x[15] ^= rotateLeft(x[11] + x[7], 18)", x);

                //  Even Rounds
                //  quarterRound(x[0], x[1], x[2], x[3]);	// row 1
//                System.out.println("Rounds " + (j + 1));
                x[1] ^= rotateLeft(x[0] + x[3], 7);
//                printWord("x[1] ^= rotateLeft(x[0] + x[3], 7)", x);

                x[2] ^= rotateLeft(x[1] + x[0], 9);
//                printWord("x[2] ^= rotateLeft(x[1] + x[0], 9)", x);

                x[3] ^= rotateLeft(x[2] + x[1], 13);
//                printWord("x[3] ^= rotateLeft(x[2] + x[1], 13)", x);

                x[0] ^= rotateLeft(x[3] + x[2], 18);
//                printWord("x[0] ^= rotateLeft(x[3] + x[2], 18)", x);

                //  quarterRound(x[5], x[6], x[7], x[4]);	// row 2
                x[6] ^= rotateLeft(x[5] + x[4], 7);
//                printWord("x[6] ^= rotateLeft(x[5] + x[4], 7)", x);

                x[7] ^= rotateLeft(x[6] + x[5], 9);
//                printWord("x[7] ^= rotateLeft(x[6] + x[5], 9)", x);

                x[4] ^= rotateLeft(x[7] + x[6], 13);
//                printWord("x[4] ^= rotateLeft(x[7] + x[6], 13)", x);

                x[5] ^= rotateLeft(x[4] + x[7], 18);
//                printWord("x[5] ^= rotateLeft(x[4] + x[7], 18)", x);

                //  quarterRound(x[10], x[11], x[8], x[9]);	// row 3
                x[11] ^= rotateLeft(x[10] + x[9], 7);
//                printWord("x[11] ^= rotateLeft(x[10] + x[9], 7)", x);

                x[8] ^= rotateLeft(x[11] + x[10], 9);
//                printWord("x[8] ^= rotateLeft(x[11] + x[10], 9)", x);

                x[9] ^= rotateLeft(x[8] + x[11], 13);
//                printWord("x[9] ^= rotateLeft(x[8] + x[11], 13)", x);

                x[10] ^= rotateLeft(x[9] + x[8], 18);
//                printWord("x[10] ^= rotateLeft(x[9] + x[8], 18)", x);

                //  quarterRound(x[15], x[12], x[13], x[14]);	// row 4
                x[12] ^= rotateLeft(x[15] + x[14], 7);
//                printWord("x[12] ^= rotateLeft(x[15] + x[14], 7)", x);

                x[13] ^= rotateLeft(x[12] + x[15], 9);
//                printWord("x[13] ^= rotateLeft(x[12] + x[15], 9)", x);

                x[14] ^= rotateLeft(x[13] + x[12], 13);
//                printWord("x[14] ^= rotateLeft(x[13] + x[12], 13)", x);

                x[15] ^= rotateLeft(x[14] + x[13], 18);
//                printWord("x[15] ^= rotateLeft(x[14] + x[13], 18)", x);

            }
            for (int j = 0; j < x.length; j++) {
                x[j] += in[j];
                int[] a = storeLittleEndian(x[j]);
//                for (int k = 0; k < 4; k++) {
//                    ks[index + (j * 4) + k] = a[k];
//                }
                System.arraycopy(a, 0, ks, index + (j * 4), 4);
            }
        }
        return ks;
    }

    public void printWord(String header, int[] array) {
        System.out.println(header);
        for (int k = 0; k < array.length; k++) {
            System.out.print(String.format("%08x", array[k]) + "\t");
            if (k % 4 == 3) {
                System.out.println("");
            }
            if (k == array.length - 1) {
                System.out.println("");
            }
        }
    }

    public void printWord(String header, String[] array) {
        System.out.println(header);
        for (int k = 0; k < array.length; k++) {
            System.out.print(array[k] + " ");
            if (k % 4 == 3) {
                System.out.println("");
            }
            if (k == array.length - 1) {
                System.out.println("");
            }
        }
    }

    public void encryptSalsa20(String img, String encImg) throws IOException {
        BufferedImage image = ImageIO.read(new File(img));//pembacaan gambar yang akan dienkripsi
        FileOutputStream fo;
        BufferedOutputStream bo;
        int width, height, idx, d = 0, r, g, b;
        int[] pixelRGB, encPixelRGB;

        width = image.getWidth();
        height = image.getHeight();
        pixelRGB = new int[width * height * 3];
        encPixelRGB = new int[width * height * 3];
        /* Pembacaan RGB per pixel pada gambar */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                idx = d * 3;
                Color color = new Color(image.getRGB(j, i));
                r = color.getRed() & 0xFF;
                g = color.getGreen() & 0xFF;
                b = color.getBlue() & 0xFF;
                pixelRGB[idx] = r;
                pixelRGB[idx + 1] = g;
                pixelRGB[idx + 2] = b;
                d++;
            }
        }
        /* ------------------------------------------ */
 /* Fungsi enkripsi salsa20/20 pada nilai RGB */
        for (int i = 0; i < pixelRGB.length / 3; i++) {
            int index = i * 3;
            encPixelRGB[index] = pixelRGB[index] ^ keyStream[index];
            encPixelRGB[index + 1] = pixelRGB[index + 1] ^ keyStream[index + 1];
            encPixelRGB[index + 2] = pixelRGB[index + 2] ^ keyStream[index + 2];
        }
        /* ----------------------------------------- */
        d = 0;
        /* Penyimpanan nilai RGB yang telah dienkripsi */
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                idx = d * 3;
                r = encPixelRGB[idx];
                g = encPixelRGB[idx + 1];
                b = encPixelRGB[idx + 2];

                Color newColor = new Color(r, g, b);
                image.setRGB(j, i, newColor.getRGB());
                d++;
            }
        }
        /* ------------------------------------------- */
        fo = new FileOutputStream(encImg);
        bo = new BufferedOutputStream(fo);
        ImageIO.write(image, "png", bo);//penyimpanan gambar hasil enkripsi
        bo.close();
    }

    public void decryptSalsa20(String encImg, String decImg) throws IOException {
        BufferedImage image = ImageIO.read(new File(encImg));//pembacaan gambar yang akan didekripsi
        FileOutputStream fo;
        BufferedOutputStream bo;
        int width, height, idx, d = 0, r, g, b;
        int[] encPixelRGB, decPixelRGB;

        width = image.getWidth();
        height = image.getHeight();
        encPixelRGB = new int[width * height * 3];
        decPixelRGB = new int[width * height * 3];
        /*Pembacaan RGB per pixel pada gambar*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                idx = d * 3;
                Color color = new Color(image.getRGB(j, i));
                r = color.getRed() & 0xFF;
                g = color.getGreen() & 0xFF;
                b = color.getBlue() & 0xFF;
                encPixelRGB[idx] = r;
                encPixelRGB[idx + 1] = g;
                encPixelRGB[idx + 2] = b;
                d++;
            }
        }

        /*Fungsi dekripsi simon 48/72 pada nilai RGB*/
//        for (int i = 0; i < encPixelRGB.length / 2; i++) {
//            int index = i * 2;
//            x = encPixelRGB[index];
//            y = encPixelRGB[index + 1];
//            for (int j = rounds - 1; j >= 0; j--) {
//                tmp = y;
//                y = (x ^ (rotateLeft(y, 1) & rotateLeft(y, 8)) ^ rotateLeft(y, 2) ^ key[j]) & 0xFFFFFF;//y = x XOR ((S^1)y AND (S^8)y) XOR (S^2)y XOR k[i]
//                x = tmp;//x = y
//            }
//            decPixelRGB[index] = x;
//            decPixelRGB[index + 1] = y;
//        }
        for (int i = 0; i < encPixelRGB.length / 3; i++) {
            int index = i * 3;
            decPixelRGB[index] = encPixelRGB[index] ^ keyStream[index];
            decPixelRGB[index + 1] = encPixelRGB[index + 1] ^ keyStream[index + 1];
            decPixelRGB[index + 2] = encPixelRGB[index + 2] ^ keyStream[index + 2];
        }

        d = 0;
        /*Penyimpanan nilai RGB yang telah didekripsi*/
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                idx = d * 3;
                r = decPixelRGB[idx];
                g = decPixelRGB[idx + 1];
                b = decPixelRGB[idx + 2];

                Color newColor = new Color(r, g, b);
                image.setRGB(j, i, newColor.getRGB());
                d++;
            }

        }
        /*-------------------------------------------*/
        fo = new FileOutputStream(decImg);
        bo = new BufferedOutputStream(fo);
        ImageIO.write(image, "png", bo);//penyimpanan gambar hasil dekripsi
        bo.close();
    }

    public int rotateRight(int n, int s, int bits) {
        return ((n >>> s) | (n << (32 - s)));//Rotasi nilai bit RGB per pixel ke kanan
    }

    public int rotateLeft(int n, int s) {
        return ((n << s) | (n >>> (32 - s)));//Rotasi nilai bit RGB per pixel ke kiri
    }

}
