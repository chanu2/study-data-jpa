package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username,int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) " +
            "from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names")List<String> names);

    List<Member> findListByUsername(String username);
    Member findMemberByUsername(String username);
    Optional<Member> findOptionalByUsername(String username);

    //Page<Member> findByAge(int age, Pageable pageable);


    //페이징
    @Query(value = "select m from Member m left join m.team t",
    countQuery = "select count(m) from Member m")
    Page<Member> findByAge(int age,Pageable pageable);

    //벌크 연산

    @Modifying(clearAutomatically = true)  // jpa excuteUpdate 처럼 사용
    @Query("update Member m set m.age = m.age +1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    //fetch join
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
    //JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    //메서드 이름으로 쿼리에서 특히 편리하다.
    @EntityGraph(attributePaths = {"team"})
    List<Member> findByUsername(String username);


    //jpahint  //
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly",value = "true"))  // 스냅샷을 생성하지 않는다
    Member findReadOnlyByUsername(String username);

    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);

    List<UsernameOnlyDto> findProjections2ByUsername(@Param("username") String username);

    <T>List<T> findProjections3ByUsername(@Param("username") String username,Class<T> type);

    //native 쿼리 projection
    @Query(value = "select m.member_id as id, m.username, t.name as teamName "+
    "from member m left join team t",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

}