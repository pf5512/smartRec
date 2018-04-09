package com.thousandsunny.ueditor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.file.Files.newInputStream;
import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * Created by guitarist on 6/5/16.
 *
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@RestController
public class UeditorController {
    @RequestMapping("/ueditor")
    public void ueditor(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding("utf-8");
            response.setHeader("Content-Type", "text/html");
            String exec = new MyActionEnter(request, newInputStream(getFile(CLASSPATH_URL_PREFIX + "/config.json").toPath())).exec();
            String contextPath = request.getContextPath();
//            response.getOutputStream().write(exec.replaceAll("\"url\": \"", "\"url\": \"" + contextPath).getBytes());
            response.getOutputStream().write(exec.replaceAll("\"url\": \"", "\"url\": \"" + "/public").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
