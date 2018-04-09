package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.DocumentFile;

/**
 * Created by guitarist on 4/13/16.
 */
public interface DocumentFileRepository extends BaseRepository<DocumentFile> {

    DocumentFile findTop1ByPath(String path);
}
