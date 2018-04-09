package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.JobType;
import com.thousandsunny.service.repository.JobTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_NO_MEMBER;

/**
 * Created by admin on 2016/10/26.
 */
@Service
public class JobTypeService extends BaseService<JobType> {

    private static final String[] TREE_JSON = {"id", "name:text", "isLevel"};

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JobTypeRepository jobTypeRepository;

    public List<JSONObject> jobTypeTree(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<JobType> jobTypes = jobTypeRepository.findByIsDeleteAndParentJobTypeIsNull(NO);
        return parseJobType(jobTypes, 0);
    }

    private List<JSONObject> parseJobType(List<JobType> parent, Integer level) {
        final Integer finalLevel = level++;
        final Integer childLevel = level;
        List<JSONObject> jsonObjects = simpleMap(parent, (jobType -> {
            JSONObject jo = propsFilter(jobType, TREE_JSON);
            jo.put("level", finalLevel);
            List<JobType> childs = jobTypeRepository.findByParentJobTypeIdAndIsDelete(jobType.getId(),NO);
            childs = simpleFilter(childs,x -> x.getIsDelete() == NO);
            jo.put("children", parseJobType(childs, childLevel));
            return jo;
        }));
        return jsonObjects;
    }

    public List<JobType> getParentJobClassList() {
        return jobTypeRepository.findByParentJobTypeIsNullAndIsDelete(NO);
    }

    public List<JobType> getChildJobClassList(Long id) {
        return jobTypeRepository.findByParentJobTypeIdAndIsDelete(id, NO);
    }

    public void persistJobType(Long id, String parentsChannel, String channelName) {
        JobType jobType = null;
        JobType parent = null;
        if (id == null) {
            jobType = new JobType();
        } else {
            jobType = jobTypeRepository.findOne(id);
        }

        if (parentsChannel != null) {
            String[] arr = parentsChannel.split(",");
            String parentStr = arr[arr.length - 1];
            Long parentId = Long.parseLong(parentStr);
            parent = jobTypeRepository.findOne(parentId);
        }
        jobType.setParentJobType(parent);
        jobType.setName(channelName);
        jobType.setDate(new Date());
        jobTypeRepository.save(jobType);
    }

    public void delJobType(Long id) {
        JobType jobType = jobTypeRepository.findOne(id);
        jobType.setIsDelete(YES);
    }

    public JobType findJobType(Long id) {
        return jobTypeRepository.findOne(id);
    }
}
