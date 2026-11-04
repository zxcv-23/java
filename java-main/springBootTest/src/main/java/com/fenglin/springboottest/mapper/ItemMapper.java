package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 条目 Mapper
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 分级分类搜索。
     * <ul>
     *   <li>keyword：模糊匹配 标题/描述/标签/正文（为空则忽略）</li>
     *   <li>categoryId：限定到某分类，并自动包含其全部子孙分类（为空则忽略）</li>
     * </ul>
     */
    @Select("<script>" +
            "SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, " +
            "       i.category_id AS categoryId, c.name AS categoryName " +
            "FROM item i " +
            "LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE i.deleted = 0 AND i.status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (i.title LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.description LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.tag LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND i.category_id IN (" +
            "    WITH RECURSIVE sub AS (" +
            "      SELECT id FROM category WHERE id = #{categoryId} " +
            "      UNION ALL " +
            "      SELECT cc.id FROM category cc INNER JOIN sub ON cc.parent_id = sub.id" +
            "    ) SELECT id FROM sub" +
            "  ) " +
            "</if>" +
            "ORDER BY i.id DESC LIMIT 100" +
            "</script>")
    List<ItemSearchVO> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    /**
     * 首页推荐：上线条目按 ID 倒序取前 8 条。
     */
    @Select("SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, " +
            "       i.category_id AS categoryId, c.name AS categoryName " +
            "FROM item i LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE i.deleted = 0 AND i.status = 1 " +
            "ORDER BY i.id DESC LIMIT 8")
    List<ItemSearchVO> featured();

    /**
     * 热门标签：去重取 item 表中的标签（最多 12 个）。
     */
    @Select("SELECT DISTINCT tag FROM item " +
            "WHERE deleted = 0 AND tag IS NOT NULL AND tag != '' " +
            "LIMIT 12")
    List<String> hotTags();

    /**
     * 后台管理列表（分页）：按关键词 / 分类 / 状态 / 删除标记筛选。
     *
     * <p>{@code deleted} 由 Service 规范化为 0（正常）或 1（回收站），
     * 这里是手写 SQL，不受 MyBatis-Plus @TableLogic 影响，因此能查到已删除的行。</p>
     */
    @Select("<script>" +
            "SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, i.content, i.author, i.status, " +
            "       i.category_id AS categoryId, c.name AS categoryName " +
            "FROM item i " +
            "LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE i.deleted = #{deleted} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (i.title LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.tag LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND i.category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "  AND i.status = #{status} " +
            "</if>" +
            "ORDER BY i.id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<ItemManageVO> managePage(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  @Param("status") Integer status,
                                  @Param("deleted") Integer deleted,
                                  @Param("offset") long offset,
                                  @Param("size") int size);

    /** 与 managePage 完全相同的筛选条件，用于分页总数 */
    @Select("<script>" +
            "SELECT COUNT(*) FROM item i " +
            "WHERE i.deleted = #{deleted} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (i.title LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.tag LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND i.category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "  AND i.status = #{status} " +
            "</if>" +
            "</script>")
    long manageCount(@Param("keyword") String keyword,
                     @Param("categoryId") Long categoryId,
                     @Param("status") Integer status,
                     @Param("deleted") Integer deleted);

    /** 回收站条目数（@TableLogic 会过滤掉，必须手写） */
    @Select("SELECT COUNT(*) FROM item WHERE deleted = 1")
    long countDeleted();

    /** 恢复逻辑删除的条目；返回受影响行数，0 表示不存在或无需恢复 */
    @Update("UPDATE item SET deleted = 0, update_time = NOW() WHERE id = #{id} AND deleted = 1")
    int restore(@Param("id") Long id);

    /**
     * 后台查询单条：按 ID 取一条未删除条目（编辑保存 / 恢复后回显用）。
     */
    @Select("SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, i.content, i.author, i.status, " +
            "       i.category_id AS categoryId, c.name AS categoryName " +
            "FROM item i " +
            "LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE i.deleted = 0 AND i.id = #{id}")
    ItemManageVO manageItem(@Param("id") Long id);
}
