package com.thousandsunny.cms.domain.controller;

import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.cms.domain.service.CommentaryService;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.entity.Result.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 如果这些代码有用，那它们是guitarist在8/4/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/baseCommentaries", produces = APPLICATION_JSON_UTF8_VALUE)
public class CommentaryController {

    @Autowired
    private CommentaryService commentaryService;

    /**
     * 发表评论
     */
    @RequestMapping(method = POST)
    public Result<Commentary> save(Commentary commentary) {
        return OK(commentaryService.saveCommentary(commentary));
    }

    /**
     * 查看评论
     */
    @RequestMapping(value = "/{articleId}", method = GET)
    public Result<Commentary> find(@PathVariable Long articleId, PageVO pageVO) {
        return OK(commentaryService.findByArticleId(articleId, pageVO.pageRequest()));
    }

    /**
     * 删除评论
     */
    @RequestMapping(value = "/{id}", method = DELETE)
    public Result<Commentary> delete(@PathVariable Long id) {
        return OK(commentaryService.delete(id));
    }
}
