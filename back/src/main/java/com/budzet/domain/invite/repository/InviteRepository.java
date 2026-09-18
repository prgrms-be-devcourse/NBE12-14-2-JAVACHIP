package com.budzet.domain.invite.repository;

import com.budzet.domain.invite.entity.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InviteRepository extends JpaRepository<Invite, String> {

    List<Invite> findAllByRoom_Id(Long roomId);
}