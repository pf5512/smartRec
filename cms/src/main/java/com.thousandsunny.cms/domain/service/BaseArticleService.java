package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.domain.repository.ArticleRepository;
import com.thousandsunny.cms.dto.SysArticle;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.ArticleReq;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.DocumentFileService;
import com.thousandsunny.core.model.DocumentFile;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.cms.ModuleKey.MoveCopy;
import static com.thousandsunny.cms.ModuleKey.MoveCopy.COPY;
import static com.thousandsunny.cms.ModuleKey.MoveCopy.MOVE;
import static com.thousandsunny.cms.ModuleTips.TIP_NOT_CHANNEL;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType.DOCUMENT;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static java.sql.Date.valueOf;
import static java.time.LocalDate.now;
import static java.util.Collections.reverse;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.isEmpty;


@Service
public class BaseArticleService extends BaseService<Article> {

    @Autowired
    private BaseChannelService baseChannelService;
    @Autowired
    private DocumentFileService documentFileService;
    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 栏目下的新闻列表
     */
    public List<Article> findByChannelId(Long channelId) {
        if (isNull(channelId))
            return newArrayList();
        Specification<Article> specification = (rt, rq, rb) -> {
            rq.where(
                    rb.equal(rt.get("channel"), baseChannelService.findOne(channelId)),
                    rb.equal(rt.get("isDelete"), NO),
                    rb.or(
                            rb.or(
                                    rb.equal(rt.get("contributed"), NO),
                                    rb.isNull(rt.get("contributed"))
                            ),
                            rb.and(
                                    rb.equal(rt.get("contributed"), YES),
                                    rb.equal(rt.get("audited"), YES)
                            )
                    )
            );
            rq.orderBy(new OrderImpl(rt.get("weight"), false), new OrderImpl(rt.get("publishTime"), false));
            return rq.getRestriction();
        };
        return articleRepository.findAll(specification);
    }

    public List<Article> findByChannelIdAndContributed(Long channelId) {
        return articleRepository.findByChannelIdAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(channelId, NO, YES);
    }

    public List<Article> findByName(String name) {
        return articleRepository.findByTitle(name);
    }

    /**
     * 栏目下的新闻
     */
    public Page<Article> findByChannelId(Long channelId, Pageable pageable) {
        ifNullThrow(baseChannelService.findOne(channelId), TIP_NOT_CHANNEL);
        return articleRepository.findByChannelIdAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(channelId, NO, YES, pageable);
    }

    public Article updateToTopArticle(long articleId) {
        Article article = findOne(articleId);
        if (article.getTop() == NO || article.getTop() == null) {
            article.setTop(YES);
        } else {
            article.setTop(NO);
        }
        return save(article);
    }

    public void updateManyToTopArticle(String idArry) {
        String[] ids = idArry.split(",");
        List<String> idList = Arrays.asList(ids);
        idList.forEach(e -> {
            Long id = Long.parseLong(e);
            Article oldArticle = articleRepository.findOne(id);
            oldArticle.setTop(YES);
            articleRepository.save(oldArticle);
        });
    }

    public Article updateRecommendArticle(long articleId) {
        Article article = findOne(articleId);
        if (article.getRecommend() == NO || article.getRecommend() == null) {
            article.setRecommend(YES);
        } else {
            article.setRecommend(NO);
        }
        return save(article);
    }

    public void updateManyRecommendArticle(String idArry) {
        String[] ids = idArry.split(",");
        List<String> idList = Arrays.asList(ids);
        idList.forEach(e -> {
            Long id = Long.parseLong(e);
            Article oldArticle = articleRepository.findOne(id);
            oldArticle.setRecommend(YES);
            articleRepository.save(oldArticle);
        });
    }

    public void deleteArticleFake(Long articleId) {
        if (articleRepository.findOne(articleId) != null) {
            Article oldArticle = articleRepository.findOne(articleId);
            oldArticle.setDeleteDate(new Date());
            oldArticle.setIsDelete(YES);
            articleRepository.save(oldArticle);
        }

    }

    /**
     * 批量删除(1,2,4,5)
     */
    public void delManyArticle(String idArray) {
        String[] ids = idArray.split(",");
        List<Long> idList = newArrayList(ids).stream().map(Long::new).collect(toList());
        idList.forEach(e -> {
            Article oldArticle = articleRepository.findOne(e);
            oldArticle.setDeleteDate(new Date());
            oldArticle.setIsDelete(YES);
            articleRepository.save(oldArticle);
        });
    }

    public Result<Article> findAllTrash() {
        return OK(articleRepository.findByIsDeleteOrderByDeleteDateDesc(YES).stream().map(e -> new SysArticle(e)).collect(toList()));
    }

    public void delManyArticleFromTrash(String idString) {
        String[] ids = idString.split(",");
        List<String> idList = Arrays.asList(ids);
        idList.forEach(e -> {
            articleRepository.delete(Long.parseLong(e));
        });
    }

    public void rebackAricle(Long id) {
        Article oldArticle = articleRepository.findOne(id);
        if (oldArticle != null) {
            oldArticle.setIsDelete(NO);
            oldArticle.setDeleteDate(null);
        }
    }

    public void rebackManyAricle(String idString) {
        String[] ids = idString.split(",");
        List<String> idList = Arrays.asList(ids);
        idList.forEach(e -> {
            Article oldArticle = articleRepository.findOne(Long.parseLong(e));
            oldArticle.setIsDelete(NO);
            oldArticle.setDeleteDate(null);
        });
    }


    public Result<Article> srarchTrash(String keyWord) {
        return OK(articleRepository.findByIsDeleteAndTitleContaining(YES, keyWord).stream().map(e -> new SysArticle(e)).collect(Collectors.toList()));
    }

    public void updateMoveOrCopyArticle(String articleIds, Long channelId, String f) {
        MoveCopy flag;
        if (f.equals("copy")) {
            flag = COPY;
        } else {
            flag = MOVE;
        }
        String[] ads = articleIds.split(",");
        Arrays.asList(ads).forEach(a -> {
            Article article = findOne(Long.parseLong(a));
            if (MOVE == flag) {
                Channel channel = new Channel();
                channel.setId(channelId);
                article.setChannel(channel);
                save(article);
            } else {
                Channel channel = new Channel();
                channel.setId(channelId);
                Article article1 = new Article();
                article1.setAudited(article.getAudited());
                article1.setChannel(channel);
                article1.setContent(article.getContent());
                article1.setSummary(article.getSummary());
                article1.setRecommend(article.getRecommend());
                article1.setAuthor(article.getAuthor());
                article1.setIntroduction(article.getIntroduction());
                article1.setKeywords(article.getKeywords());
                article1.setMechanism(article.getMechanism());
                article1.setProperty(article.getProperty());
                article1.setPublishorName(article.getPublishorName());
                article1.setPublishTime(article.getPublishTime());
                article1.setShortTitle(article.getShortTitle());
                article1.setTitle(article.getTitle());
                article1.setSource(article.getSource());
                article1.setStatus(article.getStatus());
                article1.setTop(article.getTop());
                article1.setWeight(article.getWeight());
                article1.setAudited(YES);
                save(article1);
            }

        });

    }

    /**
     * 栏目的子栏目_每个栏目前20篇
     */
    public List<Article> findTop20UnderChannel(Channel channel) {
        return articleRepository.findTop20ByChannelAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(channel, NO, YES);
    }

    /**
     * 保存资讯,封面,下载附件
     */
    public Article save(Article article, ArticleReq dto) {
        return parseArticle(article, dto);
    }

    /**
     * 更新资讯,封面,下载附件
     */
    public Article update(Article article, ArticleReq artDto) {
        Article oldArticle = articleRepository.findOne(article.getId());
        ifNotNullThen(oldArticle.getCoverImage(), article::setCoverImage);
        ifNotNullThen(oldArticle.getDownloadFile(), article::setDownloadFile);
        return parseArticle(article, artDto);
    }

    /**
     * 删除封面
     */
    public void deleteImg(Long id) {
        Article oldArticle = articleRepository.findOne(id);
        DocumentFile documentFile = oldArticle.getCoverImage();
        oldArticle.setCoverImage(null);
        if (documentFile != null) {
            documentFileService.deleteDocumentFile(documentFile.getId());
        }
        articleRepository.save(oldArticle);
    }

    /**
     * 更新保存和更新的article,artDto 是附件的信息
     */
    private Article parseArticle(Article article, ArticleReq artDto) {
        article.setPublishTime(artDto.getPublishTime_());
        MultipartFile coverImageFile = artDto.getCoverImageFile();
        MultipartFile[] rawDownFile = new MultipartFile[]{artDto.getRawDownFile()};
        String channelClassy = artDto.getChannelClassy();
        String arrId = artDto.getArrId();
        String arrText = artDto.getArrText();
        article.setAccessory(parseAccessories(arrId, arrText));

        if (!isNull(coverImageFile) && !coverImageFile.isEmpty()) {//封面
            DocumentFile coverImage = documentFileService.saveDocumentFile(coverImageFile, IMAGE);
            article.setCoverImage(coverImage);
        }
        if (fileArrayNotEmpty(rawDownFile)) {//可下载文件
            List<DocumentFile> downloadFiles = documentFileService.saveDocumentFiles(rawDownFile, DOCUMENT);
            article.setDownloadFile(downloadFiles);
        }
        if (isNotBlank(channelClassy)) {//所属栏目
            List<Long> channelIds = newArrayList(channelClassy.split(",")).stream().map(Long::new).collect(toList());
            reverse(channelIds);
            article.setChannel(new Channel(channelIds.get(0)));
        }
        if (isNull(article.getWeight())) {
            Long maxWeight = articleRepository.findMaxWeight(article.getChannel().getId());
            if (maxWeight == null) {
                maxWeight = 0l;
            }
            Long weight = (maxWeight / 10 + 1) * 10;
            article.setWeight(weight);
        }

        ifNullThen(article.getPublishTime(), () -> article.setPublishTime(new Date()));
        ifNullThen(article.getAudited(), () -> article.setAudited(NO));
        return articleRepository.save(article);
    }

    /**
     * 将1,2,3和text{-}text格式的文本解析
     */
    private List<DocumentFile> parseAccessories(String arrId, String arrText) {
        if (isEmpty(arrId))
            return null;
        String[] ids = arrId.split(",");
        String[] texts = arrText.split("\\{-\\}");
        List<DocumentFile> files = newArrayList();
        if (texts.length < ids.length) {
            for (int i = 0; i < texts.length; i++) {
                DocumentFile file = documentFileService.findOne(new Long(ids[i]));
                file.setDescription(texts[i]);
                files.add(file);
            }
            for (int j = texts.length; j < ids.length; j++) {
                DocumentFile file = documentFileService.findOne(new Long(ids[j]));
                file.setDescription(null);
                files.add(file);
            }
        } else {
            for (int k = 0; k < texts.length; k++) {
                DocumentFile file = documentFileService.findOne(new Long(ids[k]));
                file.setDescription(texts[k]);
                files.add(file);
            }
        }
        return files;
    }

    private boolean fileArrayNotEmpty(MultipartFile[] rawDownFile) {
        List<MultipartFile> list = newArrayList(rawDownFile);
        return rawDownFile.length > 0
                && list.stream().anyMatch(f -> f != null)
                && !list.stream().anyMatch(MultipartFile::isEmpty);
    }

    /**
     * 栏目的子栏目所有新闻的前20
     */
    public List<Article> findTop20UnderChannels(List<Long> ids) {
        return articleRepository.findTop20ByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(ids, NO, YES);
    }

    /**
     * 是否审核的切换
     */
    public Article toggleAudited(Long id) {
        Article article = findOne(id);
        if (article.getAudited() == null || article.getAudited() == NO)
            article.setAudited(YES);
        else
            article.setAudited(NO);
        return save(article);
    }


    public List<Article> findByChannelIds(List<Long> channelIds) {
        return articleRepository.findByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(channelIds, NO, YES);
    }

    public Page<Article> findByChannelIds(List<Long> channelIds, Pageable pageable) {
        return articleRepository.findByChannelIdInAndIsDeleteAndAuditedOrderByTopDescRecommendDescWeightDescPublishTimeDesc(channelIds, NO, YES, pageable);
    }

    /**
     * 相关新闻
     */
    public List<Article> relatedRecommendation(Long id) {
        Article article = findOne(id);
        String keywords = article.getKeywords();
        if (isNull(keywords)) return newArrayList();
        Long channelId = article.getChannel().getId();
        return articleRepository.findByKeywordsAndChannelIdAndAuditedAndIdNotInOrderByTopDescRecommendDescWeightDescPublishTimeDesc(keywords, channelId, YES, newArrayList(id));
    }

    public Page<Article> titleOrContentLikeAndChannelIdIn(String kw, List<Long> idContainer, PageRequest pageRequest, String type) {
        if (type.equals("title")) {
            Specification specification = (root, cq, cb) -> cb.and(
                    cb.or(
                            cb.like(root.get("title"), "%" + kw + "%")
                    ),
                    root.get("channel").get("id").in(idContainer)
            );
            return articleRepository.findAll(specification, pageRequest);
        } else {
            Specification specification = (root, cq, cb) -> cb.and(
                    cb.or(
                            cb.like(root.get("content"), "%" + kw + "%")
                    ),
                    root.get("channel").get("id").in(idContainer)
            );
            return articleRepository.findAll(specification, pageRequest);
        }
    }

    public Long findMaxWeight(Long id) {
        return articleRepository.findMaxWeight(id);
    }

    public Result<SysArticle> search(Long channelId, String search) {
        List<SysArticle> articles = articleRepository.findByChannelIdAndIsDeleteAndTitleContainingOrderByWeightDescPublishTimeDesc(channelId, NO, search)
                .stream().map(SysArticle::new).collect(toList());
        return OK(articles);
    }

    public void upMove(Long id, String upId) {
        if (!upId.equals("undefined")) {
            Article article = articleRepository.findOne(id);
            Article upArticle = articleRepository.findOne(Long.parseLong(upId));
            Long weight = article.getWeight();
            article.setWeight(upArticle.getWeight());
            upArticle.setWeight(weight);
            articleRepository.save(article);
            articleRepository.save(upArticle);
        }

    }

    public void downMove(Long id, String downId) {
        if (!downId.equals("undefined")) {
            Article article = articleRepository.findOne(id);
            Article downArticle = articleRepository.findOne(Long.parseLong(downId));
            Long weight = article.getWeight();
            article.setWeight(downArticle.getWeight());
            downArticle.setWeight(weight);
            articleRepository.save(article);
            articleRepository.save(downArticle);
        }

    }

    /**
     * 栏目下推荐的新闻
     */
    public Page<Article> findRecByChannelId(Long id, PageRequest pageRequest) {
        return articleRepository.findByChannelIdAndRecommendAndIsDeleteAndAuditedOrderByWeightDescPublishTimeDesc(id, YES, NO, YES, pageRequest);
    }

    /**
     * 栏目下子栏目推荐的新闻
     */
    public List<Article> findRecByParentChannelId(Long pid) {
        List<Long> childChannelsIds = simpleMap(baseChannelService.notDeletedChild(pid), Channel::getId);
        List<Article> articles = newArrayList();
        childChannelsIds.forEach(id -> articles.addAll(findRecByChannelId(id, new PageRequest(1, 1)).getContent()));
        return articles;
    }

    public Long todayCount() {
        return articleRepository.countByPublishTimeAfter(valueOf(now()));
    }

}
