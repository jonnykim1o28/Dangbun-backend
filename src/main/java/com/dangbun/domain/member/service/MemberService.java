package com.dangbun.domain.member.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.dto.request.DeleteMemberRequest;
import com.dangbun.domain.member.dto.request.DeleteSelfFromPlaceRequest;
import com.dangbun.domain.member.dto.response.GetMemberResponse;
import com.dangbun.domain.member.dto.response.GetMembersResponse;
import com.dangbun.domain.member.dto.response.GetWaitingMembersResponse;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.exception.custom.MemberNotFoundException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;


    @Transactional(readOnly = true)
    public GetMembersResponse getMembers(Long placeId) {

        Map<Member, List<String>> memberMap = new HashMap<>();

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsTrue(placeId);
        for (Member member : members) {
            List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
            List<String> dutyNames = new ArrayList<>();
            for (MemberDuty memberDuty : memberDuties) {
                dutyNames.add(memberDuty.getDuty().getName());
            }
            memberMap.put(member, dutyNames);
        }
        return GetMembersResponse.of(memberMap);
    }


    @Transactional(readOnly = true)
    public GetMemberResponse getMember(Long placeId, Long memberId) {
        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

        List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
        List<Duty> duties = new ArrayList<>();
        for (MemberDuty memberDuty : memberDuties) {
            Duty duty = memberDuty.getDuty();
            duties.add(duty);
        }

        return GetMemberResponse.of(member, duties);
    }

    @Transactional(readOnly = true)
    public GetWaitingMembersResponse getWaitingMembers(User user, Long placeId) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        List<Member> members = memberRepository.findByPlace_PlaceIdAndStatusIsFalse(placeId);

        return GetWaitingMembersResponse.of(members);

    }

    public void registerMember(User user, Long placeId, Long memberId) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

        member.activate();
    }

    public void removeWaitingMember(User user, Long placeId, Long memberId) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }
        Member member = getMemberByMemberIdAndPlaceId(memberId,placeId);

        memberRepository.delete(member);
    }

    public void exitPlace(User user, Long placeId, DeleteSelfFromPlaceRequest request) {
        Member member = memberRepository.findByUser_UserIdAndPlace_PlaceId(user.getUserId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));

        if (member.getRole() == MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        memberRepository.delete(member);
    }

    public void removeMember(User user, Long placeId, Long memberId, DeleteMemberRequest request) {

        if(getMemberByUserAndPlace(user.getUserId(), placeId).getRole() != MemberRole.MANAGER){
            throw new InvalidRoleException(INVALID_ROLE);
        }
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(()-> new MemberNotFoundException(NO_SUCH_MEMBER));

        if(!member.getName().equals(request.memberName())){
            throw new RequestRejectedException("삭제하고자 하는 맴버와 이름이 일치하지 않습니다");
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllByMember(member);
        memberDutyRepository.deleteAll(memberDuties);

        memberRepository.delete(member);
    }

    private Member getMemberByUserAndPlace(Long userId, Long placeId) {
        return memberRepository.findByUser_UserIdAndPlace_PlaceId(userId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));
    }

    private Member getMemberByMemberIdAndPlaceId(Long memberId, Long placeId){
        return memberRepository.findByMemberIdAndPlace_PlaceId(memberId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));
    }



}
