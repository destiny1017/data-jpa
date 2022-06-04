package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberJpaRepositoryTest {

    @Autowired private MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember() {
        Member member = new Member("member1");
        Member saveMember = memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.find(member.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);
        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findUsernameAndGreaterThen() {
        Member userA = new Member("userA", 10);
        Member userB = new Member("userA", 20);
        memberJpaRepository.save(userA);
        memberJpaRepository.save(userB);

        List<Member> result = memberJpaRepository.findUsernameAndGreaterThen("userA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("userA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() {
        Member memberA = new Member("memberA");
        Member memberB = new Member("memberB");

        memberJpaRepository.save(memberA);
        memberJpaRepository.save(memberB);

        List<Member> result = memberJpaRepository.findByUsername("memberA");
        Member member = result.get(0);

        assertThat(member).isEqualTo(memberA);

    }

    @Test
    public void paging() {
        memberJpaRepository.save(new Member("memberA", 10));
        memberJpaRepository.save(new Member("memberB", 10));
        memberJpaRepository.save(new Member("memberC", 10));
        memberJpaRepository.save(new Member("memberD", 10));
        memberJpaRepository.save(new Member("memberE", 10));

        int age = 10;
        int offset = 1;
        int limit = 3;

        List<Member> pageResult = memberJpaRepository.findByPage(age, offset, limit);
        Long totalCount = memberJpaRepository.totalCount(age);

        assertThat(pageResult.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);

    }
}
