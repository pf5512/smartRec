package com.thousandsunny.thirdparty.wechat.util;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import static javax.imageio.ImageIO.read;
import static jp.sourceforge.qrcode.util.ContentConverter.convert;

public class QRCodeDecoderUtil {

    public static String processDecode(String filename) {
        QRCodeDecoder decoder = new QRCodeDecoder();
        BufferedImage image;
        String decodedString = null;
        try {
            if (filename.startsWith("http://"))
                image = read(new URL(filename));
            else
                image = read(new File(filename));
            decodedString = new String(decoder.decode(new J2SEImage(image)));
            decodedString = convert(decodedString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedString;
    }

}

class J2SEImage implements QRCodeImage {
    private BufferedImage image;

    J2SEImage(BufferedImage source) {
        this.image = source;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }
}
