package com.thousandsunny;

import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.model.BaseUserDerails;
import com.thousandsunny.core.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.config.SecurityConfig.daoAuthenticationProviderInstance;
import static com.thousandsunny.core.config.SecurityConfig.plaintextPasswordEncoder;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Created by guitarist on 4/30/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MemberRepository memberRepository;

    public UserDetailsService userDetailsService() {
        return mobile -> {
//            if (isBase64(mobile.getBytes())) {
//                try {
//                    String decodedName = new String(decode(mobile.getBytes()), "utf-8");
//                    if (pureEnglishNum(decodedName) || pureChinese(decodedName))
//                        mobile = decodedName;
//                    decodedName = decodePathVariable(decodedName);
//                    if (pureEnglishNum(decodedName) || pureChinese(decodedName))
//                        mobile = decodedName;
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            }
//            Member member = memberRepository.findByUsernameAndIsDeleteAndValid(mobile, NO, YES);
            Member member = memberRepository.findByMobileOrHpAccountAndIsDelete(mobile, mobile, NO);
            if (isNull(member))
                throw new UsernameNotFoundException("用户名或密码错误!");
            else {
                String key = member.getMobile() == null ? member.getHpAccount() : member.getMobile();
                return new BaseUserDerails(key, member.getPassword(), emptyList(), member.getSalt(), member.getRole());
            }
//                return new BaseUserDerails(member.getMobile(), member.getPassword(), newArrayList(new SimpleGrantedAuthority("admin")), member.getSalt(), member.getRole());
        };
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().headers().frameOptions().disable()
                .and()
                .addFilter(digestAuthenticationFilter())
                .exceptionHandling().authenticationEntryPoint(new DigestAuthenticationEntryPoint())
                .and()
                .logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .and()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/view/**").permitAll()
                .antMatchers("/api/portal/**").permitAll()
                .antMatchers("/api/manager/members/login").permitAll()
                .antMatchers("/api/manager/members/kaptcha").permitAll()
                .antMatchers(POST, "/webArticles/contactUs").authenticated()
                .antMatchers(POST, "/commentaries").authenticated()
                .antMatchers(GET, "/baseMembers/nickName/**").authenticated()
                .antMatchers(POST, "/guestBook/commentary").authenticated();
//                .antMatchers("/api/manager/**").hasAuthority("admin");
    }

    private DigestAuthenticationEntryPoint digestEntryPoint() {
        DigestAuthenticationEntryPoint digestAuthenticationEntryPoint = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntryPoint.setKey("mykey");
        digestAuthenticationEntryPoint.setRealmName("myrealm");
        digestAuthenticationEntryPoint.setNonceValiditySeconds(3600);
        return digestAuthenticationEntryPoint;
    }

    private DigestAuthenticationFilter digestAuthenticationFilter() throws Exception {
        DigestAuthenticationFilter digestAuthenticationFilter = new DigestAuthenticationFilter();
        digestAuthenticationFilter.setAuthenticationEntryPoint(digestEntryPoint());
        digestAuthenticationFilter.setUserDetailsService(userDetailsService());
        digestAuthenticationFilter.setPasswordAlreadyEncoded(true);
        return digestAuthenticationFilter;
    }

    @Autowired
    private DaoAuthenticationProvider provider;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(provider);
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        return daoAuthenticationProviderInstance(userDetailsService(), plaintextPasswordEncoder());
    }

}
