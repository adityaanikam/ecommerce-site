package com.ecommerce.testdata;

import com.ecommerce.model.User;
import com.ecommerce.model.Address;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class UserTestDataBuilder {
    private String id;
    private String email = "test@example.com";
    private String firstName = "John";
    private String lastName = "Doe";
    private String phone = "+1234567890";
    private Set<User.Role> roles = Set.of(User.Role.USER);
    private List<Address> addresses = List.of();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private Boolean isActive = true;
    private User.AuthProvider provider = User.AuthProvider.LOCAL;
    private String imageUrl;

    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }

    public UserTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserTestDataBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public UserTestDataBuilder withRoles(Set<User.Role> roles) {
        this.roles = roles;
        return this;
    }

    public UserTestDataBuilder withAddresses(List<Address> addresses) {
        this.addresses = addresses;
        return this;
    }

    public UserTestDataBuilder withProvider(User.AuthProvider provider) {
        this.provider = provider;
        return this;
    }

    public UserTestDataBuilder withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public UserTestDataBuilder asAdmin() {
        this.roles = Set.of(User.Role.ADMIN);
        return this;
    }

    public UserTestDataBuilder asSeller() {
        this.roles = Set.of(User.Role.SELLER);
        return this;
    }

    public UserTestDataBuilder inactive() {
        this.isActive = false;
        return this;
    }

    public User build() {
        return User.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .roles(roles)
                .addresses(addresses)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .isActive(isActive)
                .provider(provider)
                .imageUrl(imageUrl)
                .build();
    }
}
