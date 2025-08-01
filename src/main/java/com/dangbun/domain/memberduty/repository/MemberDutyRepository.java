package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberDutyRepository extends JpaRepository<MemberDuty, MemberDutyId> {

    List<MemberDuty> findAllByDuty(Duty duty);

    @Query("select md from MemberDuty md join fetch md.duty where md.member = :member")
    List<MemberDuty> findAllByMember(Member member);

    boolean existsByDutyAndMember(Duty duty, Member member);

    @Query("SELECT md.member FROM MemberDuty md WHERE md.duty = :duty")
    List<Member> findMembersByDuty(Duty duty);

}
