package com.playbit.backend.webPush;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushSubscription, Long> {

    List<WebPushSubscription> findByMemberMemberId(Long id);
}
