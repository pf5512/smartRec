package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.CloudFile;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public interface CloudFileRepository extends BaseRepository<CloudFile> {
    CloudFile findTop1ByPath(String path);
}
