package com.thousandsunny.service.service;

import com.thousandsunny.cms.domain.repository.CommentaryRepository;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.CloudFileService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleTips;
import com.thousandsunny.service.model.Video;
import com.thousandsunny.service.model.VideoCollect;
import com.thousandsunny.service.model.VideoCommentary;
import com.thousandsunny.service.model.VideoFavorites;
import com.thousandsunny.service.repository.VideoCollectRepository;
import com.thousandsunny.service.repository.VideoCommentaryRepository;
import com.thousandsunny.service.repository.VideoFavoritesRepository;
import com.thousandsunny.service.repository.VideoRepository;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static com.thousandsunny.core.ModuleKey.FileType.VIDEO;
import static com.thousandsunny.service.ModuleTips.TIP_NO_COMMENTARY;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;

/**
 * 、
 * <p>
 * Created by admin on 2016/10/12.
 */
@Service
public class VideoService extends BaseService<Video> {
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private VideoFavoritesRepository videoFavoritesRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private VideoCommentaryRepository videoCommentaryRepository;
    @Autowired
    private VideoCollectRepository videoCollectRepository;
    @Autowired
    private CommentaryRepository commentaryRepository;
    @Autowired
    private CloudFileService cloudPicService;

    public Page<Video> listVideos(Pageable pageable) {
        return videoRepository.findByIsDeleteOrderByDateDesc(NO, pageable);

    }


    public Boolean isFavorite(String userToken, Long videoId) {
        VideoFavorites videoFavorites = videoFavoritesRepository.findByMemberTokenAndVideoId(userToken, videoId);
        if (videoFavorites == null || videoFavorites.getFavoriteEver() == YES)
            return false;
        else return true;
    }

    public Boolean isCollect(String userToken, Long videoId) {
        VideoCollect videoCollect = videoCollectRepository.findByMemberTokenAndVideoId(userToken, videoId);
        if (videoCollect == null || videoCollect.getCollectEver() == YES)
            return false;
        else return true;
    }

/*    public List<Boolean> isVideosFavorite(String userToken){
        List<Video> videos=videoRepository.findAll();
        List<Boolean> isFavorites=new ArrayList<>();
        for(Video video:videos){
            VideoFavorites videoFavorites=videoFavoritesRepository.findByMemberTokenAndVideoId(userToken,video.getId());
            if(videoFavorites==null||videoFavorites.getFavoriteEver()== ModuleKey.BooleanEnum.YES)
                isFavorites.add(false);
            else isFavorites.add(true);
        }
        return isFavorites;
    }

    public List<Boolean> isVideosCollected(String userToken){
        List<Video> videos=videoRepository.findAll();
        List<Boolean>isCollected=new ArrayList<>();
        for(Video video:videos){
            VideoCommentary videoCommentary=videoCommentaryRepository.findByMemberTokenAndVideoId(userToken,video.getId());
            if(videoCommentary==null||videoCommentary.getFavoriteEver()== ModuleKey.BooleanEnum.YES)
                isCollected.add(false);
            else isCollected.add(true);
        }
        return isCollected;
    }*/

    public Page<Video> searchedVideoList(String keyword, Pageable pageable) {
        Page<Video> videos = videoRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        return videos;
    }

    public String operateFavoriteVideo(String userToken, Long id, OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, ModuleKey.BooleanEnum.NO);
        ifNullThrow(member, ModuleTips.TIP_NO_MEMBER);
        Video video = videoRepository.findOne(id);
        ifNullThrow(video, ModuleTips.TIP_NO_VIDEO);
        VideoFavorites videoFavorites = videoFavoritesRepository.findByMemberIdAndVideoId(member.getId(), id);
        if (operatorType == OperatorType.SURE) {
            ifTrueThrow((videoFavorites != null && videoFavorites.getFavoriteEver() == ModuleKey.BooleanEnum.NO), ModuleTips.TIP_FAVORITE);
            if (videoFavorites == null) {
                videoFavorites = new VideoFavorites();
                videoFavorites.setMember(member);
                videoFavorites.setVideo(video);
                videoFavorites.setFavoriteDate(new Date());
                videoFavoritesRepository.save(videoFavorites);
            } else {
                videoFavorites.setFavoriteEver(ModuleKey.BooleanEnum.NO);
                videoFavorites.setFavoriteDate(new Date());
            }

        } else {
            ifNullThrow(videoFavorites, ModuleTips.TIP_NOT_FAVORITE);
            ifTrueThrow((videoFavorites.getFavoriteEver() == YES), ModuleTips.TIP_CANCEL_FAVORITE);
            videoFavorites.setFavoriteEver(YES);
        }
        return "success";
    }

    public String operateCollectedVideo(String userToken, Long id, OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, ModuleKey.BooleanEnum.NO);
        ifNullThrow(member, ModuleTips.TIP_NO_MEMBER);
        Video video = videoRepository.findOne(id);
        ifNullThrow(video, ModuleTips.TIP_NO_VIDEO);
        VideoCollect videoCollect = videoCollectRepository.findByMemberTokenAndVideoId(userToken, id);
        if (operatorType == OperatorType.SURE) {
            ifTrueThrow((videoCollect != null && videoCollect.getCollectEver() == ModuleKey.BooleanEnum.NO), ModuleTips.TIP_COLLECT);
            if (videoCollect == null) {
                videoCollect = new VideoCollect();
                videoCollect.setVideo(video);
                videoCollect.setMember(member);
                videoCollect.setDate(new Date());
                videoCollectRepository.save(videoCollect);
            } else {
                videoCollect.setCollectEver(ModuleKey.BooleanEnum.NO);
                videoCollect.setDate(new Date());
            }

        } else {
            ifNullThrow(videoCollect, ModuleTips.TIP_NOT_COLLECT);
            ifTrueThrow((videoCollect.getCollectEver() == YES), ModuleTips.TIP_CANCEL_COLLECT);
            videoCollect.setCollectEver(YES);
        }
        return "success";
    }

    public Page<VideoFavorites> getMember_video_favorite(Long id, Pageable pageable) {
        return videoFavoritesRepository.findByVideoIdAndFavoriteEver(id, ModuleKey.BooleanEnum.NO, pageable);
    }

    public Page<VideoCommentary> getMember_video_comment(Long id, Pageable pageable) {
        return videoCommentaryRepository.findByVideoIdOrderByDateDesc(id, pageable);
    }

    public VideoCommentary comment(String userToken, Long id, String content) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, ModuleKey.BooleanEnum.NO);
        ifNullThrow(member, ModuleTips.TIP_NO_MEMBER);
        Video video = videoRepository.findOne(id);
        ifNullThrow(video, ModuleTips.TIP_NO_VIDEO);
        Commentary commentary = new Commentary();
        commentary.setContent(content);
        commentary.setCommentator(member);
        commentaryRepository.save(commentary);
        VideoCommentary videoCommentary = new VideoCommentary();
        videoCommentary.setMember(member);
        videoCommentary.setCommentary(commentary);
        videoCommentary.setDate(new Date());
        videoCommentary.setVideo(video);
        VideoFavorites videoFavorites = videoFavoritesRepository.findByMemberTokenAndVideoId(userToken, id);
        if (videoFavorites != null && videoFavorites.getFavoriteEver() == YES)
            videoCommentary.setFavoriteEver(YES);
        return videoCommentaryRepository.save(videoCommentary);
    }

    public List<VideoFavorites> Top10Favorites(Long id) {
        return videoFavoritesRepository.findTop10ByVideoIdAndFavoriteEverOrderByFavoriteDate(id, ModuleKey.BooleanEnum.NO);
    }

    public List<VideoFavorites> getVideoFavorites(Long id) {
        return videoFavoritesRepository.findByVideoIdAndFavoriteEver(id, ModuleKey.BooleanEnum.NO);
    }

    //下面都是managerApi
    public Page<Video> listVideos(Pageable pageable, String text) {

        Specification<Video> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("title"), "%" + t + "%"), rb.like(rt.get("content"), "%" + t + "%"), rb.like(rt.get("videoType"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return videoRepository.findAll(specification, pageable);

    }


    public void videoDelete(Long id) {
        Video video = videoRepository.findOne(id);
        video.setIsDelete(YES);
        videoRepository.save(video);
    }

    public Video addVideo(Video video, Date createTime, String logoKey, String videoKey) {
        if (isNotNull(logoKey)) {
            CloudFile logoFile = new CloudFile();
            logoFile.setPath(logoKey);
            logoFile.setType(IMAGE);
            cloudPicService.save(logoFile);
            video.setLogo(logoFile);
        }
        if (isNotNull(videoKey)) {
            CloudFile videoFile = new CloudFile();
            videoFile.setPath(videoKey);
            videoFile.setType(VIDEO);
            cloudPicService.save(videoFile);
            video.setVideo(videoFile);
        }

        video.setDate(isNotNull(createTime) ? createTime : new Date());

        return videoRepository.save(video);

    }


    public Page<VideoFavorites> getVideoFavorites(Long id, Pageable pageable, String text) {

        Specification<VideoFavorites> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("favoriteEver"), NO));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("video").get("title"), "%" + t + "%"), rb.like(rt.get("video").get("content"), "%" + t + "%"), rb.like(rt.get("video").get("videoType"), "%" + t + "%"))));
            ifNotNullThen(id, t -> predicates.add(rb.equal(rt.get("video").get("id"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("favoriteDate"), false)).getRestriction();
        };
        return videoFavoritesRepository.findAll(specification, pageable);

    }


    public Page<VideoCommentary> comment(Long id, Pageable pageable, String text) {

        Specification<VideoCommentary> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("video").get("title"), "%" + t + "%"), rb.like(rt.get("video").get("content"), "%" + t + "%"), rb.like(rt.get("video").get("videoType"), "%" + t + "%"))));
            ifNotNullThen(id, t -> predicates.add(rb.equal(rt.get("video").get("id"), t)));
            predicates.add(rb.equal(rt.get("favoriteEver"), NO));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return videoCommentaryRepository.findAll(specification, pageable);
    }

    public void enabled(Long videoId, Long id) {
        VideoCommentary videoCommentary = videoCommentaryRepository.findByVideoIdAndCommentaryId(videoId, id);
        ifNullThrow(videoCommentary, TIP_NO_COMMENTARY);
        videoCommentary.setFavoriteEver(videoCommentary.getFavoriteEver() == NO ? YES : NO);
        videoCommentaryRepository.save(videoCommentary);

    }

    public Integer countLikeCount(Long id) {
        return videoFavoritesRepository.countByVideoIdAndFavoriteEver(id, NO);
    }

    public void deleteVideo(String userToken, Long id) {
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        VideoCommentary videoCommentary = videoCommentaryRepository.findByIdAndMember(id, member);
        ifNullThrow(videoCommentary, TIP_NO_COMMENTARY);
        videoCommentary.setFavoriteEver(YES);
        videoCommentaryRepository.save(videoCommentary);
    }
}
