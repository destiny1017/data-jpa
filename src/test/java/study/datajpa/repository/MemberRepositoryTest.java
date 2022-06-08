package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.UsernameOnlyDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("member1");
        Member saveMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findUsernameAndGreaterThen() {
        Member userA = new Member("userA", 10);
        Member userB = new Member("userA", 20);
        memberRepository.save(userA);
        memberRepository.save(userB);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("userA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("userA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member memberA = new Member("memberA");
        Member memberB = new Member("memberB");

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> result = memberRepository.findByUsername("memberA");
        Member member = result.get(0);

        assertThat(member).isEqualTo(memberA);

    }

    @Test
    public void testQuery() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> result = memberRepository.findUser("memberA", 10);
        Member member = result.get(0);

        assertThat(member).isEqualTo(memberA);

    }

    @Test
    public void testFindNameList() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<String> result = memberRepository.findUsernameList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void testFindMemberDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 20, teamA);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }

    }

    @Test
    public void testFindNames() {
        Member memberA = new Member("memberA", 10);
        Member memberB = new Member("memberB", 20);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        List<Member> result = memberRepository.findByNames(Arrays.asList("memberA", "memberB"));

        for (Member s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("memberA", 10));
        memberRepository.save(new Member("memberB", 10));
        memberRepository.save(new Member("memberC", 10));
        memberRepository.save(new Member("memberD", 10));
        memberRepository.save(new Member("memberE", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> pageResult = memberRepository.findByAge(age, pageRequest);
        Page<MemberDto> dtoPage = pageResult.map(m -> new MemberDto(m.getId(), m.getUsername(), null));

        // then
        List<Member> content = pageResult.getContent();

        assertThat(content.size()).isEqualTo(3);
        assertThat(pageResult.getTotalPages()).isEqualTo(2);
        assertThat(pageResult.getTotalElements()).isEqualTo(5);
        assertThat(pageResult.getNumber()).isEqualTo(0);
        assertThat(pageResult.isFirst()).isTrue();
        assertThat(pageResult.hasNext()).isTrue();

    }

    @Test
    public void bulkAgePlusTest() {
        // given
        memberRepository.save(new Member("memberA", 11));
        memberRepository.save(new Member("memberB", 14));
        memberRepository.save(new Member("memberC", 21));
        memberRepository.save(new Member("memberD", 20));
        memberRepository.save(new Member("memberE", 40));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        Member member = memberRepository.findByUsername("memberE").get(0);
        System.out.println("member = " + member);

        // then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() throws Exception {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));
        em.flush();
        em.clear();

        //when
        List<Member> members = memberRepository.findAll();

        //then
        for (Member member : members) {
            System.out.println("member.teamname = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHintTest() {
        //given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
        em.flush(); //Update Query 실행X
    }

    @Test
    public void customRepositoryTest() {
        // given
        memberRepository.save(new Member("memberA", 11));
        memberRepository.save(new Member("memberB", 14));
        memberRepository.save(new Member("memberC", 21));

        // when
        List<Member> member = memberRepository.findMemberCustom();

        assertThat(member.size()).isEqualTo(3);

    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        Member member1 = new Member("member1", 20);
        memberRepository.save(member1);

//        Thread.sleep(100);
        member1.setUsername("member2");

        em.flush();
        em.clear();

        Member member = memberRepository.findById(member1.getId()).get();

        System.out.println("member.getCreatedDate() = " + member.getCreatedDate());
        System.out.println("member.getUpdatedDate() = " + member.getLastModifiedDate());
        System.out.println("member.getCreatedBy() = " + member.getCreatedBy());
        System.out.println("member.getLastModifiedBy() = " + member.getLastModifiedBy());
    }

    @Test
    public void basic() throws Exception {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        //when
        //Probe 생성
        Member member = new Member("m1");
        Team team = new Team("teamA"); //내부조인으로 teamA 가능
        member.setTeam(team);
        //ExampleMatcher 생성, age 프로퍼티는 무시
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");
        Example<Member> example = Example.of(member, matcher);
        List<Member> result = memberRepository.findAll(example);
        //then
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void projections() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        em.clear();

        //when
        List<NestedClosedProjection> result = memberRepository.findProjectionsByUsername("m1", NestedClosedProjection.class);

        for (NestedClosedProjection rs : result) {
            System.out.println("rs = " + rs);
            System.out.println("rs.getUsername = " + rs.getUsername());
        }

    }

    @Test
    public void nativeQueryTest() {
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);
        em.persist(new Member("m1", 0, teamA));
        em.persist(new Member("m2", 0, teamA));
        em.flush();
        em.clear();

        // when

        Page<MemberProjection> nativeProjection = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = nativeProjection.getContent();
        
        // then
        for (MemberProjection memberProjection : content) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }
    }
}
