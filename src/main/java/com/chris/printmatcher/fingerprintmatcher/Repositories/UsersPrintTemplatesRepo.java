package com.chris.printmatcher.fingerprintmatcher.Repositories;

import com.chris.printmatcher.fingerprintmatcher.Model.UsersDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersPrintTemplatesRepo extends JpaRepository<UsersDetails, Long> {
}
