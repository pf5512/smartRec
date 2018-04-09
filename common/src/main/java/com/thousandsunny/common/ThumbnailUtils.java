package com.thousandsunny.common;

import org.junit.Test;

import java.io.IOException;

import static net.coobird.thumbnailator.Thumbnails.of;

/**
 * Created by guitarist on 5/27/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class ThumbnailUtils {

    public static void generateThumbnails(String originName) {
        try {
            String fileName = originName.split("\\.")[0];
            String fileType = originName.split("\\.")[1];
            of(originName).scale(1f).outputQuality(0.1f).outputFormat(fileType).toFile(fileName + "_thumb." + fileType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
       generateThumbnails("/Users/guitarist/Documents/work/片客/gs1.jpg");
    }
}
