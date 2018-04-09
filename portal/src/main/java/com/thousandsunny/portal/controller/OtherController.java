package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.Channel;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.model.LinePaymentBank;
import com.thousandsunny.service.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.thousandsunny.common.ChineseUtil.firstLetter;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.PhoneType;
import static com.thousandsunny.core.ModuleKey.PhoneType.IOS;
import static com.thousandsunny.service.ModuleTips.TIP_PLACE_FALSE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 * 其他
 */
@RestController
@RequestMapping(value = "/api/portal/others", produces = APPLICATION_JSON_UTF8_VALUE)
public class OtherController {

    private String[] terrace_activity_list_json = {
            "id",
            "date",
            "logo.path:imageUrl",
            "content:title"
    };

    private String[] terrace_activity_class_list_json = {
            "id",
            "name:className"
    };

    private String[] line_pament_bank_list_json = {
            "bankName",
            "bankNo:NO",
            "payee"
    };

    private String[] hot_city_list_json = {
            "id:cityId",
            "name:cityName",
            "parent.id:provinceId",
            "parent.name:provinceName"
    };

    private String[] app_version_list = {
            "id",
            "title",
            "publishTime:date"
    };

    private static String[] ACTIVITY_JSON = {
            "id",
            "title",
            "publishTime:date",
            "coverImage.path:imageUrl",
    };

    @Autowired
    private TerraceActivityClassService terraceActivityClassService;
    @Autowired
    private LinePaymentbankService linePaymentbankService;
    @Autowired
    private RegionService regionService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ChannelService channelService;


    /**
     * 平台活动列表
     */
    @RequestMapping(value = "/activityList", method = GET)
    public ResponseEntity articleList(String userToken, Long classId, PageVO pageVO) {
        Member member = memberService.findByToken(userToken);
        Page<Article> articles = articleService.activityList(pageVO.pageRequest(), classId);
        Page<JSONObject> jsonObjects = articles.map(e -> {
            JSONObject jsonObject = propsFilter(e, ACTIVITY_JSON);
            jsonObject.put("isRead", articleService.judgeIsRead(member, e));
            return jsonObject;
        });
        return ok(jsonObjects);
    }

    /**
     * 平台活动类别列表
     *
     * @return
     */
    @RequestMapping(value = "/classList", method = GET)
    public ResponseEntity activityClassList() {
        JSONObject jsonObject = new JSONObject();
        List<Channel> classlist = channelService.findActivityClassList();
        List<JSONObject> classJsons = simpleMap(classlist, channel -> propsFilter(channel, terrace_activity_class_list_json));
        jsonObject.put("list", classJsons);
        return ok(jsonObject);
    }

    /**
     * 线下付款银行列表
     *
     * @return
     */
    @RequestMapping(value = "/lineBankList", method = GET)
    public ResponseEntity lineBankList() {
        List<LinePaymentBank> list = linePaymentbankService.findBankList();
        return ok(listToJson(simpleMap(list, x -> propsFilter(x, line_pament_bank_list_json))));
    }

    /**
     * 热门城市列表
     *
     * @return
     */
    @RequestMapping(value = "/cityList", method = GET)
    public ResponseEntity cityList() {
        JSONObject jsonObject = new JSONObject();
        List<Region> list = regionService.findhotCity();
        List<JSONObject> list_ = simpleMap(list, e -> propsFilter(e, hot_city_list_json));
        if (!CollectionUtils.isEmpty(list_))
            jsonObject.put("hotCityList", list_);
        else jsonObject.put("hotCityList", new ArrayList<>());
        // 获取所有城市列表
        List<Region> allCityList = regionService.findAllCity();
        jsonObject.put("allCityList", getAllCityJSON(allCityList));
        return ok(jsonObject);
    }

    public List<JSONObject> getAllCityJSON(List<Region> list) {
        List<JSONObject> allCityJSON = new ArrayList<>();
        for (char x = 'a'; x <= 'z'; x++) {
            JSONObject jo = new JSONObject();
            List<Region> someList = new ArrayList<>();
            final char finalX = x;
            list.forEach(region -> {
                char firstChar = firstLetter(region.getName()).charAt(0);
                ifTrueThen(firstChar == finalX, () -> someList.add(region));
            });
            if (!someList.isEmpty()) {
                List<JSONObject> list_ = simpleMap(someList, some -> propsFilter(some, hot_city_list_json));
                jo.put("letter", (char) (finalX - 32));
                jo.put("list", list_);
                allCityJSON.add(jo);
            }
        }
        return allCityJSON;
    }

    /**
     * 获取省市区id
     *
     * @param provinceName
     * @param cityName
     * @param areaName
     * @return
     */
    @RequestMapping(value = "/placeId", method = GET)
    public ResponseEntity placeId(String provinceName, String cityName, String areaName) {
        checkPlaceFormat(provinceName, cityName, areaName);
        JSONObject jsonObject = new JSONObject();
        ifNotNullThen(provinceName, e -> {
            Region province = regionService.findPlace(1, decodePathVariable(e));
            jsonObject.put("provinceId", province.getId());
            jsonObject.put("provinceName", province.getName());
            ifNotNullThen(cityName, x -> {
                Region city = regionService.findRegion(2, decodePathVariable(x), province.getId());
                jsonObject.put("cityId", city.getId());
                jsonObject.put("cityName", city.getName());
                ifNotNullThen(areaName, y -> {
                    Region area = regionService.findRegion(3, decodePathVariable(y), city.getId());
                    jsonObject.put("areaId", area.getId());
                    jsonObject.put("areaName", area.getName());
                });
            });
        });
        return ok(jsonObject);
    }

    public void checkPlaceFormat(String provinceName, String cityName, String areaName) {
        ifTrueThrow(provinceName == null, TIP_PLACE_FALSE);
        ifTrueThrow(provinceName != null && cityName == null && areaName != null, TIP_PLACE_FALSE);

    }

    /**
     * 20.12 城市列表搜索】
     *
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/someCityList", method = GET)
    public ResponseEntity someCityList(String keyword) {
        List<Region> list = regionService.findSomeCityList(decodePathVariable(keyword));
        return ok(listToJson(simpleMap(list, x -> propsFilter(x, hot_city_list_json))));
    }

    /**
     * 20.13 系统版本列表
     *
     * @Author xiao xue wei
     * @Date 2016/12/28
     */
    @RequestMapping(value = "/versionList", method = GET)
    public ResponseEntity versionList(PhoneType platformType) {
        JSONObject jsonObject = new JSONObject();
        List<Article> list;
        if (platformType == IOS)
            list = articleService.findArticleList(21L);
        else
            list = articleService.findArticleList(22L);
        jsonObject.put("list", simpleMap(list, e -> propsFilter(e, app_version_list)));
        return ok(jsonObject);
    }
}
