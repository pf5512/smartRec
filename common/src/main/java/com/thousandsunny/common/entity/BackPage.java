package com.thousandsunny.common.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by guitarist on 5/12/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class BackPage<T> extends PageImpl<T> {
    public BackPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public BackPage(List<T> content) {
        super(content);
    }

    public BackPage(Page<T> page) {
        this(page.getContent(), new BackPageRequest(page.getNumber(), page.getSize()), page.getTotalElements());
    }

    public boolean hasNext() {
        return this.getNumber() < this.getTotalPages();
    }

    public boolean hasPrevious() {
        return this.getNumber() > 1;
    }

}
