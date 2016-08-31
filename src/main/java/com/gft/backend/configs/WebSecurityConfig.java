package com.gft.backend.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import javax.sql.DataSource;

/**
 * Created by miav on 2016-08-19.
 */
@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

//    @Autowired
//    public DbUsersDetailsService usersDetailsService;

    @Autowired
    DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {

        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        "select username, password, enabled, id from users  " +
                                "where username=?")
                .authoritiesByUsernameQuery(
                        "select username, role from userroles_v where username=?");
    }

//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(usersDetailsService);
////        auth.inMemoryAuthentication().withUser("miav").password("123456").roles("USER");
////        auth.inMemoryAuthentication().withUser("admin").password("123456").roles("ADMIN");
////        auth.inMemoryAuthentication().withUser("dba").password("123456").roles("DBA");
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/", "/home/**", "/folder/**", "/resource/**",
                        "/js/**", "/topic/**", "/add/**").permitAll()
                .antMatchers("/admin/**").access("hasRole('ADMIN')")
                .antMatchers("/dba/**").access("hasRole('ADMIN') or hasRole('DBA')")
                .and().logout().logoutSuccessUrl("/login?logout")
                .and().formLogin().loginPage("/login")
                .usernameParameter("username").passwordParameter("password")
                .and().exceptionHandling().accessDeniedPage("/403");

//        http.authorizeRequests()
//                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
//                .antMatchers("/dba/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_DBA')")
//                .and().formLogin();
    }


}
