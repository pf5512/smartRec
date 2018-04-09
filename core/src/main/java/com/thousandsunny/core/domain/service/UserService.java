package com.thousandsunny.core.domain.service;


import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.repository.*;
import com.thousandsunny.core.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;

import static com.google.common.collect.ImmutableSortedMap.of;
import static com.thousandsunny.common.Base64ToImageUtil.decodeBase64ToImage;
import static com.thousandsunny.common.RandomNumberUtil.genSalt;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.entity.Result.notFound;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static com.thousandsunny.core.ModuleKey.SmsType.RESET_PWD;
import static com.thousandsunny.core.ModuleTips.*;
import static com.thousandsunny.core.config.SecurityConfig.encodePassword;
import static java.sql.Timestamp.from;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

/**
 * User: Lewis Wang
 * Date: 7/20/11
 * Time: 5:25 PM
 */
@Service
public class UserService extends BaseService<User> {

    @Autowired
    private SmsService smsService;
    @Autowired
    private DocumentFileService documentFileService;
    @Autowired
    private UserMsgrRepository userMsgRepository;
    @Autowired
    private MemberSignInRecordRepository userSignInRecordRepository;
    @Autowired
    private MemberScoreRepository userScoreRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DocumentFileRepository documentFileRepository;

    public BaseUserDerails userPrincipal() {
        //获取登录用户信息
        Authentication authentication = getContext().getAuthentication();
        BaseUserDerails userDerails = null;
        try {
            userDerails = (BaseUserDerails) authentication.getPrincipal();
        } catch (Exception e) {
            ifNullThrow(null, TIP_NOT_LOGIN);
        }
        return userDerails;
    }

    public User getUserFromContext() {
        //获取登录用户信息
        Authentication authentication = getContext().getAuthentication();
        BaseUserDerails userDerails = null;
        try {
            userDerails = (BaseUserDerails) authentication.getPrincipal();
        } catch (Exception e) {
            ifNullThrow(null, TIP_NOT_LOGIN);
        }
        return findByUsername(userDerails.getUsername());
    }

    public User resetPwd(String mobile, String code, String newPwd) {
        ifFalseThrow(mobileIsExist(mobile), TIP_MOBILE_NOT_FOUND);
        User user = userRepository.findByMobile(mobile);
        smsService.validateReceiverAndCode(user.getMobile(), code, RESET_PWD);
        String salt = genSalt();
        user.setPassword(encodePassword(newPwd, salt));
        user.setSalt(salt);
        baseRepository.save(user);
        smsService.sendContent(user.getMobile(), TIP_PWD_RESET.getMessage(), null, RESET_PWD);
        return user;

    }

    public User updatePwd(Long userId, String oldPwd, String newPwd) {
        User user = baseRepository.findOne(userId);
        String oldPwdHash = encodePassword(oldPwd, user.getSalt());
        ifFalseThrow(user.getPassword().equals(oldPwdHash), TIP_OLD_PWD_WRONG);

        String salt = genSalt();
        user.setPassword(encodePassword(newPwd, salt));
        user.setSalt(salt);
        baseRepository.save(user);
        smsService.sendContent(user.getMobile(), "密码修改成功！", null, RESET_PWD);
        return user;
    }


    public User regOneUser(User user) {
        ifTrueThrow(mobileIsExist(user.getMobile()), TIP_MOBILE_EXISTED);
        return baseRepository.save(initUser(user));
    }

    private User initUser(User user) {
        String salt = randomUUID().toString().substring(0, 8);
        user.setSalt(salt);
        user.setPassword(encodePassword(user.getPassword(), salt));
        user.setCreateTime(new Date());
        user.setModifyTime(new Date());
        return user;
    }


    public Boolean mobileIsExist(String mobile) {
        return findOne(mapToEqualSpec(of("mobile", mobile))) != null;
    }


    public boolean userNameIsExist(String username) {
        return findOne(mapToEqualSpec(of("username", username))) != null;
    }

    public User updatePersonalInfo(MultipartFile headImageFile, User user) {
        User oldUser = baseRepository.findOne(user.getId());
        DocumentFile headImage;
        if (headImageFile != null) {
            headImage = documentFileService.saveDocumentFile(headImageFile, IMAGE);
            oldUser.setHeadImage(headImage);
        }

        ifNotNullThen(user.getUsername(), oldUser::setUsername);
        ifNotNullThen(user.getGender(), oldUser::setGender);
        ifNotNullThen(user.getAge(), oldUser::setAge);
        ifNotNullThen(user.getResume(), oldUser::setResume);
        ifNotNullThen(user.getHeight(), oldUser::setHeight);
        ifNotNullThen(user.getWeight(), oldUser::setWeight);
        ifNotNullThen(user.getBirthday(), oldUser::setBirthday);
        oldUser.setModifyTime(new Date());
        return save(oldUser);
    }

    public Page<UserMsg> findAllMsg(Long userId, PageRequest pageRequest) {
        return userMsgRepository.findByReceiverId(userId, pageRequest);
    }

    public UserMsg readOneMsg(Long userId, Long msgId) {
        UserMsg msg = userMsgRepository.findByIdAndReceiverId(msgId, userId);
        ifNullThrow(msg, TIP_MSG_NOT_EXIST);
        ifTrueThrow(YES == msg.getIsRead(), TIP_CANT_READ_AGAIN);
        msg.setIsRead(YES);
        userMsgRepository.save(msg);
        return msg;
    }


    public Result<String> hasSignIn(Long memberId) {
        MemberCheckRecord userSignInRecord = userSignInRecordRepository.findByMemberId(memberId);
        if (isNull(userSignInRecord)) {
            return notFound("尚未签到");
        }
        Date time = userSignInRecord.getDate();
        LocalDateTime lastSignIn = from(time.toInstant()).toLocalDateTime();
        LocalDateTime today = now();
        if (lastSignIn.getDayOfYear() == today.getDayOfYear()) {
            return OK();
        }
        return notFound("尚未签到");
    }

    public MemberScore getMemberScore(Long memberId) {
        return userScoreRepository.findByMemberId(memberId);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUserSet(User user, String upImg) {
        //解码base64位图片,保存为DocumentFile
        DocumentFile documentFile = null;
        if (isNotBlank(upImg)) {
            String path = decodeBase64ToImage(upImg);
            documentFile = new DocumentFile(path);
            documentFileRepository.save(documentFile);
        }
        if (!isNull(user.getId())) {
            User oldUser = findOne(user.getId());
            ifNotNullThen(user.getHeadImage(), oldUser::setHeadImage);
            ifNotNullThen(user.getUsername(), oldUser::setUsername);
            ifNotNullThen(user.getRealName(), oldUser::setRealName);
            ifNotNullThen(user.getGender(), oldUser::setGender);
            ifNotNullThen(user.getBirthday(), oldUser::setBirthday);
            ifNotNullThen(user.getMobile(), oldUser::setMobile);
            ifNotNullThen(user.getQq(), oldUser::setQq);
            ifNotNullThen(user.getTelephone(), oldUser::setTelephone);
            ifNotNullThen(user.getEmail(), oldUser::setEmail);
            ifNotNullThen(user.getValid(), oldUser::setValid);
            ifNotNullThen(user.getResume(), oldUser::setResume);
            ifNotNullThen(documentFile, oldUser::setHeadImage);
            if (isNotBlank(user.getPassword())) {
                String salt = genSalt();
                oldUser.setSalt(salt);
                oldUser.setPassword(encodePassword(user.getPassword(), salt));
            }
            return save(oldUser);
        } else {
            ifNotNullThrow(findByUsername(user.getUsername()), TIP_USER_NAME_EXIST);
            user.setCreateTime(new Date());
            String salt = genSalt();
            user.setSalt(salt);
            user.setPassword(encodePassword(user.getPassword(), salt));
            return save(user);
        }
    }


}
