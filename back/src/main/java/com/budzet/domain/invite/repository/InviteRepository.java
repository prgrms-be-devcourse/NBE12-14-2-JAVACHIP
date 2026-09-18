package com.budzet.domain.invite.repository;

import com.budzet.domain.invite.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRepository extends JpaRepository<Invite, String> {
}