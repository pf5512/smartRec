package com.thousandsunny.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.EntityStatus;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 部门表
 */
@Entity
@Table(name = "core_org")
public class Org {

    private Long id;

    public Org() {
    }

    public Org(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Comment("部门名称")
    private String name;

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Comment("部门注解")
    private String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    @Comment("部门主管")
    private String director;

    @Column(length = 50)
    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    @Comment("部门电话")
    private String telephone;

    @Column(length = 50)
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Comment("归属部门")
    private Org parent;

    @JsonIgnore
    @ManyToOne
    public Org getParent() {
        return parent;
    }

    public void setParent(Org parent) {
        this.parent = parent;
    }

    @Comment("子部门")
    private List<Org> childOrgs;

    @OneToMany(mappedBy = "parent")
    public List<Org> getChildOrgs() {
        return childOrgs;
    }

    public void setChildOrgs(List<Org> childOrgs) {
        this.childOrgs = childOrgs;
    }

    @Comment("排序值")
    private Integer orderCode = 100;

    public Integer getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Integer orderCode) {
        this.orderCode = orderCode;
    }

    @Comment("")
    private Integer levelCode;

    public Integer getLevelCode() {
        return levelCode;
    }

    public void setLevelCode(Integer levelCode) {
        this.levelCode = levelCode;
    }


    @Comment("")
    private String pathCode;

    public String getPathCode() {
        return pathCode;
    }

    public void setPathCode(String pathCode) {
        this.pathCode = pathCode;
    }

    private Date createTime=new Date();

    private Date modifyTime;

    private String creator;

    private String modifier;

    private EntityStatus entityStatus;

    private BooleanEnum isDelete = BooleanEnum.NO;

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(BooleanEnum isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    /*
            部门下用户
         */
    private List<User> users;

    @JsonIgnore
    @OneToMany(mappedBy = "org")
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
