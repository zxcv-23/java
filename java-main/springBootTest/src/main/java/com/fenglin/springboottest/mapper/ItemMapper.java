package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.dto.ItemManageVO;
import com.fenglin.springboottest.dto.ItemSearchVO;
import com.fenglin.springboottest.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
     * 后台管理列表：返回全部未删除条目（含已下线），附带分类名称与状态。
     *
     * @param keyword     关键词（可选，匹配标题/标签）
     * @param categoryId  分类ID（可选，精确匹配直接归属分类）
     */
    @Select("<script>" +
            "SELECT i.id, i.emoji, i.title, i.description AS `desc`, i.tag, i.content, i.author, i.status, " +
            "       i.category_id AS categoryId, c.name AS categoryName " +
            "FROM item i " +
            "LEFT JOIN category c ON c.id = i.category_id " +
            "WHERE i.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (i.title LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR i.tag LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND i.category_id = #{categoryId} " +
            "</if>" +
            "ORDER BY i.id DESC LIMIT 200" +
            "</script>")
    List<ItemManageVO> manageList(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
