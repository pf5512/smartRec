package com.thousandsunny.cms;


import com.thousandsunny.common.entity.TitleEnum;

public class ModuleKey {

    public static final int CONTENT_SIZE = 500;

    public enum ContentType implements TitleEnum {
        PTZX("普通资讯");
        private String title;

        ContentType(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum MoveCopy implements TitleEnum {
        MOVE("移动"), COPY("复制");

        private String title;

        MoveCopy(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return null;
        }
    }

    public enum BrowseAuthorityEnum implements TitleEnum {
        OPEN("公开浏览"), AUTHORITY("授权浏览");
        private String title;

        BrowseAuthorityEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum ChannelTypeEnum implements TitleEnum {
        LIST("列表"), SINGLE("单页"), LINK("链接");
        private String title;

        ChannelTypeEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }


    public enum ContentPropertyEnum implements TitleEnum {
        H("头条"), C("推荐"), F("幻灯"), A("特荐"), S("滚动"), B("加粗"), P("图片"), J("跳转"), E("审核");
        private String title;

        ContentPropertyEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum PagingTypeEnum implements TitleEnum {
        AUTO("自动"), MANUAL("手动");
        private String title;

        PagingTypeEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum SiteTypeEnum implements TitleEnum {
        INFORMATION("资讯"), FUNCTION("功能"), TOPICS("专题");
        private String title;

        SiteTypeEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

    }

    public enum SiteTemplateEnum implements TitleEnum {
        TEMP1("模板一"), TEMP2("模板二"), TEMP3("模板三"), TEMP4("模板四"), TEMP5("模板五"), TEMP6("模板六"), TEMP7("模板七");
        private String title;

        SiteTemplateEnum(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum SecretLevel implements TitleEnum {
        SECRET("秘密"), INNER("内部");
        private String title;

        SecretLevel(String title) {
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public enum ImgDirection implements TitleEnum {

        SIDEWAYS("横向图"), VERTICAL("竖向图");
        private String name;

        ImgDirection(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum VisitType implements TitleEnum {
        CURRENT("当前窗口跳转"), NEWBUILD("新建窗口");
        private String name;

        VisitType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum GuestBookCheckType implements TitleEnum {
        UNCHECK("未审核"), CHECKPASS("通过"), UNCHECKPASS("未通过");
        private String name;

        GuestBookCheckType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum GuestBookReadType implements TitleEnum {
        READ("已查看"), UNREAD("未查看");
        private String name;

        GuestBookReadType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum MenuType implements TitleEnum {
        LYGL("留言管理"), ZMXL("最美系列"), SXDC("思想调查"), GGGL("广告管理"), YQLJ("友情链接");
        private String name;

        MenuType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum AdType implements TitleEnum {
        WORD("文字"), IMAGE("图片");
        private String name;

        AdType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum fLType implements TitleEnum {
        WORD("文字"), IMAGE("图片");
        private String name;

        fLType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }

    public enum TagType implements TitleEnum {
        COURSE("课程"), ARTICLE("咨讯"), SHOP("店铺"), RESUME("简历"), SCHOOL("学校");
        private String name;

        TagType(String name) {
            this.name = name;
        }

        @Override
        public String getTitle() {
            return name;
        }
    }
}
