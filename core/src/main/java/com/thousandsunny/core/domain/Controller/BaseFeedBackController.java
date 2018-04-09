package com.thousandsunny.core.domain.Controller;

import com.thousandsunny.core.model.FeedBack;
import com.thousandsunny.core.domain.service.FeedBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * 如果这些代码有用，那它们是guitarist在9/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/base/feedBack", produces = APPLICATION_JSON_UTF8_VALUE)
public class BaseFeedBackController {

    @Autowired
    private FeedBackService feedBackService;

    @RequestMapping(method = POST)
    public ResponseEntity save(FeedBack feedBack, String userToken) {
        feedBackService.save(feedBack, userToken);
        return OK;
    }
}
