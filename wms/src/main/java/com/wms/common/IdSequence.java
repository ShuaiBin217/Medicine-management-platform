package com.wms.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 删除数据后把表的自增起点收回到 max(id)+1。
 * 这样"删掉最后一条再新增"时 ID 会从小号重新开始，而不是一路往上跳。
 */
@Slf4j
@Component
public class IdSequence {

    private static final Set<String> TABLES = new HashSet<>(Arrays.asList(
            "user", "storage", "goodstype", "goods", "record"));

    @Autowired
    private DataSource dataSource;

    public void reset(String table) {
        if (!TABLES.contains(table)) {
            return;
        }
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            long next = 1L;
            try (ResultSet rs = st.executeQuery("select ifnull(max(id), 0) + 1 from `" + table + "`")) {
                if (rs.next()) {
                    next = rs.getLong(1);
                }
            }
            st.executeUpdate("alter table `" + table + "` auto_increment = " + next);
        } catch (Exception e) {
            log.warn("重置 {} 自增起点失败：{}", table, e.getMessage());
        }
    }
}
