package com.playbit.backend.webPush;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushSubscription, Long> {

    List<WebPushSubscription> findByMemberId(Long id);
}
