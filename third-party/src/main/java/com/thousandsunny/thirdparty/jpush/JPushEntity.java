package com.thousandsunny.thirdparty.jpush;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by guitarist on 7/13/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class JPushEntity {

    private ExtraEntity extraEntity;
    private String content;

    private List<String> flags;

    public JPushEntity() {
    }

    public JPushEntity(String type, String content, String extra, String... flags) {
        this.content = content;
        this.flags = newArrayList(flags);
        setExtraEntity(new ExtraEntity(type,extra));
    }

    public JPushEntity(String type, String content, String extra, List<String> flags) {
        this.content = content;
        this.flags = flags;
        setExtraEntity(new ExtraEntity(type,extra));
    }

    public ExtraEntity getExtraEntity() {
        return extraEntity;
    }

    public void setExtraEntity(ExtraEntity extraEntity) {
        this.extraEntity = extraEntity;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    private class ExtraEntity {
        private String type;
        private String extra;

        public ExtraEntity(String type, String extra) {
            this.type = type;
            this.extra = extra;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }
}
