package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

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

}
