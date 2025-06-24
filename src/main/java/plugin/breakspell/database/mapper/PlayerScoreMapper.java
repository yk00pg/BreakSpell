package plugin.breakspell.database.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import plugin.breakspell.database.data.PlayerScore;

public interface PlayerScoreMapper {

  // スコア情報を新しい順に5件選択し、古い順に並び替えてリスト形式で取得
  @Select("""
      SELECT * FROM(
       SELECT * FROM player_score
       ORDER BY id DESC
       LIMIT 5
      )AS latest_scores
      ORDER BY id ASC
      """)
  List<PlayerScore> selectNewlyScoreList();

  // スコア情報をスコアが高い順に5件リスト形式で取得
  @Select("""
      SELECT * FROM player_score
      ORDER BY score DESC
      LIMIT 5
      """)
  List<PlayerScore> selectRankedScoreList();

  // 指定の難易度のスコア情報をスコアが高い順に5件リスト形式で取得
  @Select("""
      SELECT * FROM player_score
      WHERE difficulty=#{difficulty}
      ORDER BY score DESC
      LIMIT 5
      """)
  List<PlayerScore> selectRankedByDifficultyScoreList(@Param("difficulty") String difficulty);

  // 指定の難易度のハイスコアを取得
  @Select("SELECT MAX(score) FROM player_score WHERE difficulty=#{difficulty}")
  Integer selectHighScore(@Param("difficulty") String difficulty);

  // 新しいスコアを登録
  @Insert("""
      INSERT INTO player_score(player_uuid, player_name, score, difficulty, registered_at)
      VALUES (#{playerUuid}, #{playerName}, #{score}, #{difficulty}, now())
      """)
  void insertNewScore(PlayerScore playerScore);
}