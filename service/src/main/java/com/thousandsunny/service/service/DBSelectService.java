package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.ModuleKey.IdentityType;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.repository.PositionRepository;
import com.thousandsunny.service.ModuleKey.*;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;

/**
 * Created by ekoo on 2016/11/21.
 */
@Service
public class DBSelectService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PositionRepository positionRepository;

    public Map<String, List<JSONObject>> dbSelect(String type) {

        List<JSONObject> jsonObject = null;
        Map<String, List<JSONObject>> map = new HashMap<>();
        String[] arr = type.split(",");
        for (String str : arr) {
            switch (str) {

                case "ComplainType":
                    jsonObject = getByArray(newArrayList(ComplainType.values()));    //投诉类型
                    break;
                case "EntrepreneursType":
                    jsonObject = getByArray(newArrayList(EntrepreneursType.values()));
                    break;
                case "AdCategoryEnum":
                    jsonObject = getByArray(newArrayList(AdCategoryEnum.values()));   //所属类别
                    break;
                case "AdTypeEnum":
                    jsonObject = getByArray(newArrayList(AdTypeEnum.values()));       //广告类别
                    break;
                case "PayType":
                    jsonObject = getByArray(newArrayList(PayType.values()));       //付款方式
                    break;

                case "IdentityType":
                    jsonObject = getByArray(newArrayList(IdentityType.values()));       //创业类型
                    break;

                case "SourceType":
//                    jsonObject = getByArray(newArrayList(SourceType.values()));       //操作类型
                    jsonObject = getByArray(newArrayList(JOB_NEW, JOB_ADD, JOB_CUT, JOB_RENEW, JOB_RESIGN, JOB_REFUND));
                    break;

                case "RecruitmentType":
                    jsonObject = getByArray(newArrayList(RecruitmentType.values()));       //招聘类型
                    break;

                case "RecruitmentState":
                    jsonObject = getByArray(newArrayList(RecruitmentState.values()));       //岗位状态
                    break;
//
//                case "":
//                    jsonObject = getByArray(newArrayList(.values()));       //
//                    break;

//                case "Position":
//                    jsonObject = getByList(positionRepository.findByCompanyAndIsDelete(company,BooleanEnum.NO));
//                    break;
            }

            map.put(str, jsonObject);
        }

        return map;
    }


    private List<JSONObject> getByList(List list) {

        String[] JSON = {"id", "name"};
        List<JSONObject> jsonObjectList = simpleMap(list, x -> {
            JSONObject jo = propsFilter(x, JSON);
            return jo;
        });
        return jsonObjectList;
    }


    private List<JSONObject> getByArray(List<Enum> list) {

        List<JSONObject> jsonObjects = simpleMap(list, x -> {
            String[] ENUM_JSON = {"title:text"};
            JSONObject jo = propsFilter(x, ENUM_JSON);
            jo.put("value", x.name());
            return jo;
        });
        return jsonObjects;
    }
}
