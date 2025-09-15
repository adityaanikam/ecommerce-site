package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return createUserPrincipal(user);
    }
    
    @Transactional
    public UserDetails loadUserById(String id) {
        log.debug("Loading user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
        
        if (!user.getIsActive()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }
        
        return createUserPrincipal(user);
    }
    
    private UserDetails createUserPrincipal(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                user.getIsActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user.getRoles())
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(Set<User.Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }
}