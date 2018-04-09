package com.thousandsunny.cms.domain.controller;

import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.domain.service.DocumentFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * 如果这些代码有用，那它们是guitarist在7/30/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/files")
public class DocumentFileController {

    @Autowired
    private DocumentFileService documentFileService;

    /**
     * 接受上传的附件
     */
    @RequestMapping(method = POST, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<DocumentFile> saveAccessory(MultipartFile file) {
        return OK(documentFileService.saveDocumentFile(file, IMAGE));
    }

    /**
     * 接受前台上传的附件
     */
    @RequestMapping(value= "/files", method = POST, produces = TEXT_PLAIN_VALUE)
    public String saveAccessoryFiles(MultipartFile file) {

        DocumentFile documentFile = documentFileService.saveDocumentFile(file, IMAGE);
        String url = documentFile.getUrl();
        String id = String.valueOf(documentFile.getId());
        return id + "{-}" + url;
    }

    /**
     * 删除接受附件
     */
    @RequestMapping(value = "/{id}", method = DELETE, produces = APPLICATION_JSON_UTF8_VALUE)
    public Result<DocumentFile> deleteAccessory(@PathVariable Long id) {
        documentFileService.deleteDocumentFile(id);
        return OK();
    }
}
