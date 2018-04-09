package com.thousandsunny.thirdparty.easemob.comm;

import com.thousandsunny.thirdparty.easemob.EasemobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static java.lang.Boolean.TRUE;

@Component
public class ClientContext {

    /*
     * Configuration Source Type
     */
    public static final String INIT_FROM_PROPERTIES = "FILE";

    public static final String INIT_FROM_CLASS = "CLASS";

    /*
     * Implementation List
     */
    public static final String JERSEY_API = "jersey";

    public static final String HTTPCLIENT_API = "httpclient";

    /*
     * Properties
     */
    private static final String API_PROTOCAL_KEY = "API_PROTOCAL";

    private static final String API_HOST_KEY = "API_HOST";

    private static final String API_ORG_KEY = "API_ORG";

    private static final String API_APP_KEY = "API_APP";

    private static final String APP_CLIENT_ID_KEY = "APP_CLIENT_ID";

    private static final String APP_CLIENT_SECRET_KEY = "APP_CLIENT_SECRET";

    private static final String APP_IMP_LIB_KEY = "APP_IMP_LIB";

    private static final Logger log = LoggerFactory.getLogger(ClientContext.class);

    private Boolean initialized = TRUE;

    @Autowired
    private EasemobConfig config;

    private EasemobRestAPIFactory factory;

    private TokenGenerator token; // Wrap the token generator
    private static ClientContext context;

    public ClientContext() {
    }

    @PostConstruct
    public void init() {
        ClientContext.context = this;
        token = new TokenGenerator(context);
    }

    public static ClientContext getInstance() {
        return context;
    }

    public EasemobRestAPIFactory getAPIFactory() {

        if (null == this.factory) {
            this.factory = EasemobRestAPIFactory.getInstance(this);
        }

        return this.factory;
    }

    public String getSeriveURL() {

        String serviceURL = config.getProtocal() + "://" + config.getHost() + "/" + config.getOrg() + "/" + config.getApp();

        return serviceURL;
    }

    public String getAuthToken() {
        if (null == token) {
            log.error(MessageTemplate.INVAILID_TOKEN_MSG);
            throw new RuntimeException(MessageTemplate.INVAILID_TOKEN_MSG);
        }

        return token.request(Boolean.FALSE);
    }

    private ClientContext initFromStaticClass() {
        return null;
    }


    public String getHost() {
        return config.getHost();
    }

    public String getOrg() {
        return config.getOrg();
    }

    public String getApp() {
        return config.getApp();
    }

    public Boolean isInitialized() {
        return initialized;
    }

    public String getImpLib() {
        return config.getImpLib();
    }

    public String getClientId() {
        return config.getClientId();
    }

    public String getClientSecret() {
        return config.getClientSecret();
    }
}
