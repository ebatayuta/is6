package nogi.nozaki.model;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MatchMapper {

	@Insert("INSERT INTO match_info (matchId, white, black, board, turn, state) "
			+ "VALUES (#{matchId}, #{white}, #{black}, #{board}, #{turn}, #{state})")
	void insertMatch(Match match);

	@Select("SELECT matchId, white, black, board, turn, state "
			+ "FROM match_info WHERE matchId = #{matchId}")
	Match selectByMatchId(String matchId);
}
