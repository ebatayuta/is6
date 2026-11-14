package nogi.nozaki.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

@MybatisTest
class MatchMapperTest {

	@Autowired
	private MatchMapper matchMapper;

	@Test
	void insertAndSelectReturnsSavedMatchInformation() {
		Match expected = new Match("match-001", "white-player", "black-player", "initial-board", "white", "ongoing");

		matchMapper.insertMatch(expected);
		Match actual = matchMapper.selectByMatchId(expected.getMatchId());

		assertNotNull(actual);
		assertEquals(expected.getMatchId(), actual.getMatchId());
		assertEquals(expected.getWhite(), actual.getWhite());
		assertEquals(expected.getBlack(), actual.getBlack());
		assertEquals(expected.getBoard(), actual.getBoard());
		assertEquals(expected.getTurn(), actual.getTurn());
		assertEquals(expected.getState(), actual.getState());
	}

	@Test
	void duplicateMatchIdFailsAndKeepsExistingMatchUnchanged() {
		Match original = new Match("match-002", "white-player", "black-player", "initial-board", "white", "ongoing");
		Match duplicate = new Match("match-002", "new-white", "new-black", "changed-board", "black", "finished");

		matchMapper.insertMatch(original);

		assertThrows(DataAccessException.class, () -> matchMapper.insertMatch(duplicate));

		Match actual = matchMapper.selectByMatchId(original.getMatchId());
		assertNotNull(actual);
		assertEquals(original.getWhite(), actual.getWhite());
		assertEquals(original.getBlack(), actual.getBlack());
		assertEquals(original.getBoard(), actual.getBoard());
		assertEquals(original.getTurn(), actual.getTurn());
		assertEquals(original.getState(), actual.getState());
	}
}
