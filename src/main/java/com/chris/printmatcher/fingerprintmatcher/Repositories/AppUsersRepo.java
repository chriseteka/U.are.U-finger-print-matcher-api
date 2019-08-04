package com.chris.printmatcher.fingerprintmatcher.Repositories;

import com.chris.printmatcher.fingerprintmatcher.Model.AppUsers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUsersRepo extends JpaRepository<AppUsers, Long> {

    AppUsers findDistinctByUserId(String userId);
}
