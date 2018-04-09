package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.domain.repository.CommentaryRepository;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * 如果这些代码有用，那它们是guitarist在8/4/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class CommentaryService extends BaseService<Commentary> {
    @Autowired
    private CommentaryRepository commentaryRepository;
    @Autowired
    private BaseMemberService memberService;

    public List<Commentary> findByArticleId(Long id) {
        return commentaryRepository.findByArticleIdAndState(id, YES);
    }

    public Page<Commentary> findByArticleId(Long articleId, PageRequest pageRequest) {
        return commentaryRepository.findByArticleIdAndState(articleId, YES, pageRequest);
    }

    public Commentary saveCommentary(Commentary commentary) {
        commentary.setCommentator(memberService.getMemberFromContext());
//        commentary.setIpAddress(memberService.getMemberFromContext().getLastIp());
        return save(commentary);
    }

    public void deleteMany(String ids) {
        String[] idArr = ids.split(",");
        List<String> idList = Arrays.asList(idArr);
        idList.forEach(id -> commentaryRepository.delete(Long.parseLong(id)));
    }

    public List<Commentary> search(String keyWord) {
        return commentaryRepository.findByArticleTitleContainingOrderByCreateTimeDesc(keyWord);
    }

}
