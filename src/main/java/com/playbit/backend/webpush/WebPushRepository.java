package com.playbit.backend.webpush;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushSubscription, Long> {

    List<WebPushSubscription> findByMemberMemberId(Long id);
}
