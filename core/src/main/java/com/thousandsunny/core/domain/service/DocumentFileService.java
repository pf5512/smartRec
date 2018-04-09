package com.thousandsunny.core.domain.service;

import com.thousandsunny.core.model.DocumentFile;
import com.thousandsunny.core.domain.repository.DocumentFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.core.ModuleKey.FileType;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * Created by guitarist on 4/14/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Service
public class DocumentFileService extends BaseService<DocumentFile> {

    private final Logger _logger = LoggerFactory.getLogger(getClass());

    private final static String UPLOAD_FOLDER = "upload";
    private static final String IMAGE_FOLDER = "image";
    private static final String DOCUMENT_FOLDER = "document";
    private static final String CHAT_RECORD_FOLDER = "chat_record";

    @Autowired
    private DocumentFileRepository documentFileRepository;

    public DocumentFile findOne(Long id) {
        return documentFileRepository.findOne(id);
    }

    /**
     * 下载网络图片
     */
    public DocumentFile saveDocumentFiles(String url, FileType fileType) {
        return saveToDb(saveUrlToFileSystem(url, fileType)).get(0);
    }

    private List<String> saveUrlToFileSystem(String url, FileType fileType) {
        Path uploadFolder = checkUploadFolderIsExistIfNotCreateIt(fileType);
        return newArrayList(saveToHardDisk(uploadFolder, url));
    }

    /**
     * 保存多个文件
     */
    public List<DocumentFile> saveDocumentFiles(MultipartFile[] multipartFiles, FileType fileType) {
        return saveToDb(saveToFileSystem(multipartFiles, fileType));
    }

    /**
     * 保存一个文件
     */
    public DocumentFile saveDocumentFile(MultipartFile multipartFile, FileType fileType) {
        return saveToDb(saveToFileSystem(new MultipartFile[]{multipartFile}, fileType)).get(0);
    }

    private List<DocumentFile> saveToDb(List<String> files) {
        List<DocumentFile> documentFiles = newArrayList();
        files.forEach(localPath -> {
            String url = localPath.substring(localPath.indexOf(UPLOAD_FOLDER) - 1).replaceAll("\\\\", "/");
            documentFiles.add(new DocumentFile(localPath, url));
        });
        return documentFileRepository.save(documentFiles);
    }

    private List<String> saveToFileSystem(MultipartFile[] multipartFiles, FileType fileType) {
        Path uploadFolder = checkUploadFolderIsExistIfNotCreateIt(fileType);
        return newArrayList(multipartFiles).stream().map(multipartFile -> saveToHardDisk(uploadFolder, multipartFile)).collect(toList());
    }

    private Path checkUploadFolderIsExistIfNotCreateIt(FileType fileType) {
        Path uploadFolder = null;
        try {
            File file = getFile(CLASSPATH_URL_PREFIX);
            String basePath = file.toPath().getParent().getParent().toString();
            uploadFolder = typicalFileFolder(basePath, fileType);
            if (notExists(uploadFolder)) {
                createDirectories(uploadFolder);
            }
        } catch (FileNotFoundException e) {
            _logger.warn("DocumentFileService:文件没有找到:{}", e);
        } catch (IOException e) {
            _logger.warn("DocumentFileService:文件操作异常:{}", e);
        }
        return uploadFolder;
    }

    private Path typicalFileFolder(String basePath, FileType fileType) {
        String todayFolder = now().toString();
        switch (fileType) {
            case IMAGE:
                return get(basePath, UPLOAD_FOLDER, IMAGE_FOLDER, todayFolder);
            case DOCUMENT:
                return get(basePath, UPLOAD_FOLDER, DOCUMENT_FOLDER, todayFolder);
            default:
                return get(basePath, UPLOAD_FOLDER, CHAT_RECORD_FOLDER, todayFolder);
        }
    }

    private String saveToHardDisk(Path uploadFolder, MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (!originalFilename.contains("."))
            originalFilename = originalFilename + ".jpg";
        Path fileFinalPath = get(uploadFolder.toString(), randomUUID() + originalFilename.substring(originalFilename.indexOf(".")));
        try (InputStream s = multipartFile.getInputStream()) {
            copy(s, fileFinalPath);
        } catch (IOException e) {
            _logger.warn("DocumentFileService:文件保存异常:{}", e);
        }
        return fileFinalPath.toString();
    }

    private String saveToHardDisk(Path uploadFolder, String url) {
        Path fileFinalPath = get(uploadFolder.toString(), randomUUID() + ".jpg");
        try {
            copy(new URL(url).openStream(), fileFinalPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileFinalPath.toString();
    }

    public DocumentFile saveDocumentFile(MultipartFile file, String description, FileType image) {
        DocumentFile documentFile = saveDocumentFile(file, image);
        documentFile.setDescription(description);
        documentFileRepository.save(documentFile);
        return documentFile;
    }

    public void deleteDocumentFile(Long id) {
        documentFileRepository.delete(id);
    }

    public List<DocumentFile> save(List<DocumentFile> files) {
        return documentFileRepository.save(files);
    }
}
