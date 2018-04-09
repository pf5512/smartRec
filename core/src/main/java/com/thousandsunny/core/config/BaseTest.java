package com.thousandsunny.core.config;

import com.thousandsunny.BooterApplication;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by guitarist on 5/11/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(BooterApplication.class)
@WebAppConfiguration
public class BaseTest {

    protected static Logger logger = getLogger(BaseTransactionTest.class);
    private MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setMockMvc() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    protected ResultActions performRequest(MockHttpServletRequestBuilder builder) {
        ResultActions result = null;
        try {
            result = mockMvc.perform(builder);
            logger.info(result.andReturn().getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected ResultMatcher isEqualTo(String jPath, Object value) {
        return jsonPath(jPath, equalTo(value));
    }
}

