package com.thousandsunny.ueditor.upload;

import com.thousandsunny.ueditor.PathFormat;
import com.thousandsunny.ueditor.define.AppInfo;
import com.thousandsunny.ueditor.define.BaseState;
import com.thousandsunny.ueditor.define.FileType;
import com.thousandsunny.ueditor.define.State;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BinaryUploader {

    public static final State save(HttpServletRequest request, Map<String, Object> conf) {

        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            MultipartFile multipartFile = multipartRequest.getFile(conf.get("fieldName").toString());

            String savePath = (String) conf.get("savePath");
            String originFileName = multipartFile.getOriginalFilename();
            String suffix = FileType.getSuffixByFilename(originFileName);

            originFileName = originFileName.substring(0, originFileName.length() - suffix.length());
            savePath = savePath + UUID.randomUUID().toString() + suffix;

            long maxSize = ((Long) conf.get("maxSize")).longValue();

            if (!validType(suffix, (String[]) conf.get("allowFiles"))) {
                return new BaseState(false, AppInfo.NOT_ALLOW_FILE_TYPE);
            }


            State storageState = StorageManager.saveFileByInputStream(multipartFile.getInputStream(), savePath, maxSize);


            if (storageState.isSuccess()) {
                String format = PathFormat.format(savePath);
                storageState.putInfo("url", format.substring(format.indexOf("public/") + 6));
                storageState.putInfo("type", suffix);
                storageState.putInfo("original", originFileName + suffix);
            }

            return storageState;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return new BaseState(false, AppInfo.IO_ERROR);
    }

    private static boolean validType(String type, String[] allowTypes) {
        List<String> list = Arrays.asList(allowTypes);

        return list.contains(type);
    }
}
