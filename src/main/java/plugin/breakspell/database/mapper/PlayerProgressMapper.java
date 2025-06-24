package plugin.breakspell.database.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import plugin.breakspell.database.data.PlayerProgress;

public interface PlayerProgressMapper {

  // いずれかの難易度でゲームをクリアしたことがあるかを確認
  @Select("""
      SELECT EXISTS(
      SELECT 1
      FROM player_progress
      WHERE player_uuid=#{playerUuid} AND cleared=true)
      """)
  boolean hasClearedAnyDifficulty(@Param("playerUuid") String playerUuid);

  // 指定の難易度でゲームをクリアしたことがあるかを確認
  @Select("""
      SELECT cleared
      FROM player_progress
      WHERE player_uuid=#{playerUuid} AND difficulty=#{difficulty}
      """)
  boolean hasCleared(
      @Param("playerUuid") String playerUuid, @Param("difficulty") String difficulty);

  // いずれかの難易度でゲームをプレイしたことがあるかを確認
  @Select("""
      SELECT EXISTS(
      SELECT 1
      FROM player_progress
      WHERE player_uuid=#{playerUuid} AND played=true)
      """)
  boolean hasPlayedAnyDifficulty(@Param("playerUuid") String playerUuid);

  // 指定の難易度でゲームをプレイしたことがあるかを確認
  @Select("""
      SELECT played
      FROM player_progress
      WHERE player_uuid=#{playerUuid} AND difficulty=#{difficulty}
      """)
  Boolean hasPlayed(
      @Param("playerUuid") String playerUuid, @Param("difficulty") String difficulty);

  // ゲームの進捗状況を登録
  @Insert("""
      INSERT INTO player_progress(player_uuid, difficulty, played, cleared, cleared_at)
      VALUES (#{playerUuid}, #{difficulty}, #{played}, #{cleared}, #{clearedAt})
      """)
  void insertPlayerProgress(PlayerProgress playerProgress);

  // ゲームのクリア状況を更新
  @Update("""
      UPDATE player_progress SET cleared=true, cleared_at=now()
      WHERE player_uuid=#{playerUuid} AND difficulty=#{difficulty}
      """)
  void updatePlayerProgress(
      @Param("playerUuid") String playerUuid, @Param("difficulty") String difficulty);
}