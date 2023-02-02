package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(savedMember.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);



        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);

    }
    @Test
    public void paging(){

        //given
        memberJpaRepository.save(new Member("aaaa1",10));
        memberJpaRepository.save(new Member("aaaa2",10));
        memberJpaRepository.save(new Member("aaaa3",10));
        memberJpaRepository.save(new Member("aaaa4",10));
        memberJpaRepository.save(new Member("aaaa5",10));

        //when
        int age =10;
        int offset=0;
        int limit =3;

        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);


        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);

    }


    //벌크 연산 테스트
    @Test
    public void bulkUpdate(){

        memberJpaRepository.save(new Member("aaaa1",10));
        memberJpaRepository.save(new Member("aaaa2",19));
        memberJpaRepository.save(new Member("aaaa3",20));
        memberJpaRepository.save(new Member("aaaa4",21));
        memberJpaRepository.save(new Member("aaaa5",41));

        int resultCount = memberJpaRepository.bulkAgePlus(20);

        assertThat(resultCount).isEqualTo(3);



    }
}