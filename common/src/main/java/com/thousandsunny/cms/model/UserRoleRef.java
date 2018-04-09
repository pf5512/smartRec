package com.thousandsunny.cms.model;

import com.thousandsunny.core.model.User;

import javax.persistence.*;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 7/22/16.
 */
@Entity
@Table(name = "cms_user_role_ref")
public class UserRoleRef {
    private Long id;

    private User user;

    private Role role;

    public UserRoleRef() {
    }

    public UserRoleRef(User user, Role role) {
        setUser(user);
        setRole(role);
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToOne
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
