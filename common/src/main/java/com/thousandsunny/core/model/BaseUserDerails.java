package com.thousandsunny.core.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

import static com.thousandsunny.core.ModuleKey.AccountEnum;

/**
 * Created by guitarist on 5/4/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class BaseUserDerails extends User {
    private String salt;

    private AccountEnum role;

    public AccountEnum getRole() {
        return role;
    }

    public void setRole(AccountEnum role) {
        this.role = role;
    }

    public BaseUserDerails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public BaseUserDerails(String username, String password, Collection<? extends GrantedAuthority> authorities, String salt, AccountEnum role) {
        super(username, password, authorities);
        setSalt(salt);
        setRole(role);
    }

    public BaseUserDerails(String username, String password, Collection<? extends GrantedAuthority> authorities, String salt) {
        super(username, password, authorities);
        setSalt(salt);
        setRole(role);
    }

    public BaseUserDerails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public BaseUserDerails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities, String salt) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        setSalt(salt);
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
