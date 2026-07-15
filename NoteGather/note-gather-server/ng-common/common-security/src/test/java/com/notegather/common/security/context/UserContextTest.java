package com.notegather.common.security.context;

import com.notegather.common.security.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldClearCurrentUser() {
        UserContext.set(LoginUser.builder().userId("1001").username("alice").build());

        UserContext.clear();

        assertThat(UserContext.get()).isNull();
    }
}
