package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @PersistenceContext EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember =  memberRepository.findById(savedMember.getId()).get();
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());

        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성보장
    }

    @Test
    public void findMemberDto(){
        Member m1 = new Member("aaaa",11);
        Team team = new Team("teamA");
        teamRepository.save(team);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }


    }

    @Test
    public void findByNames(){
        Member m1 = new Member("aaaa",11);
        Member m2 = new Member("bbbb",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaaa", "bbbb"));

        for (Member member : result) {
            System.out.println("member = " + member);
        }


    }

    @Test
    public void paging(){

        //given
        memberRepository.save(new Member("aaaa1",10));
        memberRepository.save(new Member("aaaa2",10));
        memberRepository.save(new Member("aaaa3",10));
        memberRepository.save(new Member("aaaa4",10));
        memberRepository.save(new Member("aaaa5",10));

        int age =10;

        PageRequest pageReauest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageReauest);

        // 실무 꿀팁
        Page<MemberDto> memberDtos = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();


    }

    @Test
    public void bulkUpdate(){

        memberRepository.save(new Member("aaaa1",10));
        memberRepository.save(new Member("aaaa2",19));
        memberRepository.save(new Member("aaaa3",20));
        memberRepository.save(new Member("aaaa4",21));
        memberRepository.save(new Member("aaaa5",41));


        // 벌크 연산시 영속성 컨텍스트에 clear를 해줘야한다  그것을 @Modifying clearAutomatically 사용해야함

        int resultCount = memberRepository.bulkAgePlus(20);

        Member aaaa5 = memberRepository.findMemberByUsername("aaaa5");

        System.out.println("aaaa5 = " + aaaa5);



        assertThat(resultCount).isEqualTo(3);



    }

    //fetch join
    @Test
    public void findMemberLazy(){
        //given
        // member1 -> teamA 참조
        // member2 -> teamB 참조

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member1", 10, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when

        List<Member> members = memberRepository.findByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
        }

        //then
    }
    @Test
    public void queryHint(){
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");
        em.flush();


    }


    @Test
    public void callCustom(){

        List<Member> memberCustom = memberRepository.findMemberCustom();

    }


    //엔티티 전체가 아닌 회원의 이름만 가져오고 싶을 때 사용하는 방법
    @Test
    public void projections() {

        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1");

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly = " + usernameOnly);
        }

    }

    //dto로
    @Test
    public void projections2() {

        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        List<UsernameOnlyDto> result = memberRepository.findProjections2ByUsername("m1");

        for (UsernameOnlyDto usernameOnlyDto : result) {
            System.out.println("usernameOnlyDto = " + usernameOnlyDto);
        }
    }


    //중첩으로 유저이름과 팀이름만 가져오고 싶을 때
    @Test
    public void projections3() {

        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        List<NestedClosedProjections> result = memberRepository.findProjections3ByUsername("m1",NestedClosedProjections.class);

        for (NestedClosedProjections nestedClosedProjections : result) {
            System.out.println("nestedClosedProjections = " + nestedClosedProjections);
            String username = nestedClosedProjections.getUsername();
            System.out.println("username = " + username);
            String name = nestedClosedProjections.getTeam().getName();
            System.out.println("name = " + name);

        }

    }

    @Test
    public void nativeQuery() {

        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        //when
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0,10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getUsername() = " + memberProjection.getTeamName());
        }


    }


}