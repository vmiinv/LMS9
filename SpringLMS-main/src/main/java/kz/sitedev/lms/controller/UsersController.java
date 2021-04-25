package kz.sitedev.lms.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import kz.sitedev.lms.entity.Role;
import kz.sitedev.lms.entity.User;
import kz.sitedev.lms.repository.RoleRepository;
import kz.sitedev.lms.repository.UserRepository;
import kz.sitedev.lms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Api
public class UsersController {
    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("")
    public List<User> all(){
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        return userRepository.findById(id).get();
    }

    @GetMapping("/find/")
    public List<User> getByNameContainingAndAge(@RequestParam String name) {
        return userRepository.findAllByNameContains(name);
    }

    @PostMapping("/register")
    public ResponseEntity create(@RequestBody User user) {
        user.setRoles(Arrays.asList(roleRepository.findFirstByName("USER")));
        userService.create(user);
        Long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                // Convert to list of strings.
                // This is important because it affects the way we get them back in the Gateway.
                .claim("authorities", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 60*60 * 1000))  // in milliseconds
                .signWith(SignatureAlgorithm.HS512, "secret-key".getBytes())
                .compact();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization",
                "Bearer " + token);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(user);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id,
                           @RequestBody User user) {
        user.setId(id);
        return userRepository.saveAndFlush(user);
    }

    @PatchMapping("/{id}")
    public User updateUserName(@PathVariable Long id,
                              @RequestParam String name) {
        User user = userRepository.findById(id).get();
        user.setName(name);
        return userRepository.saveAndFlush(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        userRepository.deleteById(id);
    }
}
