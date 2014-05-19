package com.kalixia.ha.api;

import com.kalixia.ha.api.configuration.ConfigurableService;
import com.kalixia.ha.api.configuration.UsersServiceConfiguration;
import com.kalixia.ha.model.User;
import rx.Observable;

public interface UsersService extends ConfigurableService<UsersServiceConfiguration> {
    User findByUsername(String username);
    void saveUser(User user);
    Observable<User> findUsers();
    Long getUsersCount();
}
