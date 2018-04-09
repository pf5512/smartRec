package com.thousandsunny.common;

import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.thousandsunny.common.RandomNumberUtil.genSalt;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.notExists;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * Created by mu.jie on 2016/8/15.
 */
public class Base64ToImageUtil {
    public static String decodeBase64ToImage(String imgStr) {//对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return null;
        String imgType= "."+imgStr.substring(imgStr.indexOf("/")+1,imgStr.indexOf(";"));
        String data = imgStr.substring(imgStr.indexOf(",")+1);
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(data);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }
            //生成jpeg图
            File file = getFile(CLASSPATH_URL_PREFIX);
            String basePath = file.toPath().getParent().getParent().toString();
            String imgFilePath = basePath+"\\upload\\img\\user\\headImage\\";//新生成的图片路径
            String imgName = genSalt()+imgType;//新生成的图片名称
            Path uploadFolder = Paths.get(imgFilePath);
            if (notExists(uploadFolder)){
                createDirectories(uploadFolder);
            }
            OutputStream out = new FileOutputStream(imgFilePath+imgName);
            out.write(b);
            out.flush();
            out.close();
            return imgFilePath+imgName;
        } catch (Exception e) {
            return null;
        }
    }
}
