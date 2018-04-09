package com.thousandsunny.thirdparty.easemob.service;

import com.thousandsunny.thirdparty.easemob.EasemobConfig;
import com.thousandsunny.thirdparty.easemob.api.ChatGroupAPI;
import com.thousandsunny.thirdparty.easemob.api.IMUserAPI;
import com.thousandsunny.thirdparty.easemob.comm.EasemobRestAPIFactory;
import com.thousandsunny.thirdparty.easemob.comm.body.ChatGroupBody;
import com.thousandsunny.thirdparty.easemob.comm.body.IMUserBody;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.BodyWrapper;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.thirdparty.easemob.comm.ClientContext.getInstance;
import static com.thousandsunny.thirdparty.easemob.comm.EasemobRestAPIFactory.CHATGROUP_CLASS;
import static com.thousandsunny.thirdparty.easemob.comm.EasemobRestAPIFactory.USER_CLASS;
import static org.apache.jackrabbit.util.Text.md5;

/**
 * Created by guitarist on 7/5/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Service
public class EasemobService {
    @Autowired
    private EasemobConfig easemobConfig;

    //    @Async
    public void registerEasemobUser(String token) {
        EasemobRestAPIFactory factory = getInstance().getAPIFactory();
        IMUserAPI user = (IMUserAPI) factory.newInstance(USER_CLASS);
        BodyWrapper userBody = new IMUserBody(token, password(token), token);
        user.createNewIMUserSingle(userBody);
    }


    public ResponseWrapper createChatGroup(ChatGroupBody payload) {
        payload.setOwner(easemobConfig.getDefaultOwner());
        EasemobRestAPIFactory factory = getInstance().getAPIFactory();
        ChatGroupAPI group = (ChatGroupAPI) factory.newInstance(CHATGROUP_CLASS);
        ResponseWrapper chatGroup = (ResponseWrapper) group.createChatGroup(payload);
        return chatGroup;
    }

    public void addSingleUserToChatGroup(String groupId, String username) {
        EasemobRestAPIFactory factory = getInstance().getAPIFactory();
        ChatGroupAPI group = (ChatGroupAPI) factory.newInstance(CHATGROUP_CLASS);
        group.addSingleUserToChatGroup(groupId, username);
    }

    public void removeBatchUsersFromChatGroup(String groupId, String[] strings) {
        EasemobRestAPIFactory factory = getInstance().getAPIFactory();
        ChatGroupAPI group = (ChatGroupAPI) factory.newInstance(CHATGROUP_CLASS);
        group.removeBatchUsersFromChatGroup(groupId, strings);
    }

    public void removeSingleUserFromChatGroup(String groupId, String string) {
        EasemobRestAPIFactory factory = getInstance().getAPIFactory();
        ChatGroupAPI group = (ChatGroupAPI) factory.newInstance(CHATGROUP_CLASS);
        group.removeSingleUserFromChatGroup(groupId, string);
    }


    public String password(String token) {
        return md5(easemobConfig.getAppkey() + easemobConfig.getSaltpass() + token);
    }


    private byte[] getBytes(String token) {
        return (getInstance().getApp() + token).getBytes();
    }
}
