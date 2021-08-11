package com.kzow3n.jdbcplus.core;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kzow3n.jdbcplus.core.column.AggregateWrapper;
import com.kzow3n.jdbcplus.pojo.TableInfo;
import com.kzow3n.jdbcplus.utils.ColumnUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.session.SqlSession;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sql构造器
 *
 * @author owen
 * @since 2021/8/4
 */
@Slf4j
public class SqlWrapper extends AbstractSqlWrapper {

    public SqlWrapper() {
        init();
    }

    public void initSql() {
        sql = "";
        blnFormatSql = false;
    }

    //region 拼接基本Sql

    public SqlWrapper distinct() {
        blnDistinct = true;
        return this;
    }

    public SqlWrapper selectAll(Integer... tableIndexes) {
        for (Integer tableIndex : tableIndexes) {
            TableInfo selectAllTableInfo = new TableInfo(tableIndex, true);
            selectAllTableInfos.add(selectAllTableInfo);
        }
        return this;
    }

    @SafeVarargs
    public final <K> SqlWrapper select(Integer tableIndex, SFunction<K, ?>... fns) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        for (SFunction<K, ?> fn : fns) {
            Field field = ColumnUtils.getField(fn);
            addColumnInfoByField(tableInfo, field, true, null, null);
        }
        return this;
    }

    public final <K, M> SqlWrapper select(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, null);
        return this;
    }

    public final <K> SqlWrapper select(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, null);
        return this;
    }

    public final <M> SqlWrapper select(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, null);
        return this;
    }

    public SqlWrapper select(Integer tableIndex, String tableColumns, @Nullable String beanColumns) {
        addColumnInfo(tableIndex, tableColumns, beanColumns, null);
        return this;
    }

    public SqlWrapper from(Class<?> clazz, String tableId) {
        appendFrom(clazz, tableId);
        return this;
    }

    public SqlWrapper from(Consumer<SqlWrapper> consumer, String tableId) {
        appendFrom(consumer, tableId);
        return this;
    }

    public SqlWrapper innerJoin(Class<?> clazz, String tableId) {
        appendJoin(clazz, tableId, "inner join");
        return this;
    }

    public SqlWrapper innerJoin(Consumer<SqlWrapper> consumer, String tableId) {
        appendJoin(consumer, tableId, "inner join");
        return this;
    }

    public SqlWrapper leftJoin(Class<?> clazz, String tableId) {
        appendJoin(clazz, tableId, "left join");
        return this;
    }

    public SqlWrapper leftJoin(Consumer<SqlWrapper> consumer, String tableId) {
        appendJoin(consumer, tableId, "left join");
        return this;
    }

    public SqlWrapper rightJoin(Class<?> clazz, String tableId) {
        appendJoin(clazz, tableId, "right join");
        return this;
    }

    public SqlWrapper rightJoin(Consumer<SqlWrapper> consumer, String tableId) {
        appendJoin(consumer, tableId, "right join");
        return this;
    }

    public SqlWrapper fullJoin(Class<?> clazz, String tableId) {
        appendJoin(clazz, tableId, "full join");
        return this;
    }

    public SqlWrapper fullJoin(Consumer<SqlWrapper> consumer, String tableId) {
        appendJoin(consumer, tableId, "full join");
        return this;
    }

    public <K, M> SqlWrapper on(Integer tableIndex1, SFunction<K, ?> fn1, Integer tableIndex2, SFunction<M, ?> fn2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        TableInfo tableInfo2 = getTableInfoByIndex(tableIndex2);
        String tableId1 = tableInfo1.getTableId();
        String tableId2 = tableInfo2.getTableId();
        Field field1 = ColumnUtils.getField(fn1);
        String column1 = getColumn(tableInfo1, field1);
        Field field2 = ColumnUtils.getField(fn2);
        String column2 = getColumn(tableInfo2, field2);
        appendOn(tableId1, column1, tableId2, column2);
        return this;
    }

    public <K> SqlWrapper on(Integer tableIndex1, SFunction<K, ?> fn1, Integer tableIndex2, String column2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        TableInfo tableInfo2 = getTableInfoByIndex(tableIndex2);
        String tableId1 = tableInfo1.getTableId();
        String tableId2 = tableInfo2.getTableId();
        Field field1 = ColumnUtils.getField(fn1);
        String column1 = getColumn(tableInfo1, field1);
        appendOn(tableId1, column1, tableId2, column2);
        return this;
    }

    public <M> SqlWrapper on(Integer tableIndex1, String column1, Integer tableIndex2, SFunction<M, ?> fn2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        TableInfo tableInfo2 = getTableInfoByIndex(tableIndex2);
        String tableId1 = tableInfo1.getTableId();
        String tableId2 = tableInfo2.getTableId();
        Field field2 = ColumnUtils.getField(fn2);
        String column2 = getColumn(tableInfo2, field2);
        appendOn(tableId1, column1, tableId2, column2);
        return this;
    }

    public SqlWrapper on(Integer tableIndex1, String column1, Integer tableIndex2, String column2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        TableInfo tableInfo2 = getTableInfoByIndex(tableIndex2);
        String tableId1 = tableInfo1.getTableId();
        String tableId2 = tableInfo2.getTableId();
        appendOn(tableId1, column1, tableId2, column2);
        return this;
    }

    public final <K> SqlWrapper count(SFunction<K, ?> fn) {
        Field field = ColumnUtils.getField(fn);
        String beanColumn = field.getName();
        addColumnInfo(null, "count(*)", beanColumn, null);
        return this;
    }

    public SqlWrapper count(String beanColumn) {
        addColumnInfo(null, "count(*)", beanColumn, null);
        return this;
    }

    public final <K, M> SqlWrapper count(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "count(%s)");
        return this;
    }

    public final <K> SqlWrapper count(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "count(%s)");
        return this;
    }

    public final <M> SqlWrapper count(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "count(%s)");
        return this;
    }

    public SqlWrapper count(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "count(%s)");
        return this;
    }

    public final <K, M> SqlWrapper avg(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "avg(%s)");
        return this;
    }

    public final <K> SqlWrapper avg(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "avg(%s)");
        return this;
    }

    public final <M> SqlWrapper avg(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "avg(%s)");
        return this;
    }

    public SqlWrapper avg(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "avg(%s)");
        return this;
    }

    public final <K, M> SqlWrapper sum(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "sum(%s)");
        return this;
    }

    public final <K> SqlWrapper sum(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "sum(%s)");
        return this;
    }

    public final <M> SqlWrapper sum(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "sum(%s)");
        return this;
    }

    public SqlWrapper sum(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "sum(%s)");
        return this;
    }

    public final <K, M> SqlWrapper max(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "max(%s)");
        return this;
    }

    public final <K> SqlWrapper max(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "max(%s)");
        return this;
    }

    public final <M> SqlWrapper max(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "max(%s)");
        return this;
    }

    public SqlWrapper max(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "max(%s)");
        return this;
    }

    public final <K, M> SqlWrapper min(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "min(%s)");
        return this;
    }

    public final <K> SqlWrapper min(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "min(%s)");
        return this;
    }

    public final <M> SqlWrapper min(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "min(%s)");
        return this;
    }

    public SqlWrapper min(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "min(%s)");
        return this;
    }

    public final <K, M> SqlWrapper groupConcat(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        Field field2 = ColumnUtils.getField(fn2);
        addColumnInfoByFields(tableInfo, field1, true, field2, "group_concat(%s)");
        return this;
    }

    public final <K> SqlWrapper groupConcat(Integer tableIndex, SFunction<K, ?> fn1, String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        Field field1 = ColumnUtils.getField(fn1);
        addColumnInfoByField(tableInfo, field1, true, beanColumn, "group_concat(%s)");
        return this;
    }

    public final <M> SqlWrapper groupConcat(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        Field field2 = ColumnUtils.getField(fn2);
        String beanColumn = field2.getName();
        addColumnInfo(tableIndex, tableColumn, beanColumn, "group_concat(%s)");
        return this;
    }

    public SqlWrapper groupConcat(Integer tableIndex, String tableColumn, String beanColumn) {
        addColumnInfo(tableIndex, tableColumn, beanColumn, "group_concat(%s)");
        return this;
    }

    public SqlWrapper formatSql() {
        formatFullSql();
        return this;
    }

    //endregion

    //region 条件构造器

    public SqlWrapper or() {
        blnOr = true;
        return this;
    }

    public SqlWrapper or(Consumer<SqlWrapper> consumer) {
        blnOr = true;
        spendOperator();
        sqlBuilder.append("(");
        blnOpenBracket = true;
        consumer.accept(this);
        sqlBuilder.append(") ");
        return this;
    }

    public SqlWrapper and(Consumer<SqlWrapper> consumer) {
        spendOperator();
        sqlBuilder.append("(");
        blnOpenBracket = true;
        consumer.accept(this);
        sqlBuilder.append(") ");
        return this;
    }

    public <K> SqlWrapper isNull(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        isNull(tableId, column);
        return this;
    }

    public SqlWrapper isNull(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        isNull(tableId, column);
        return this;
    }

    public <K> SqlWrapper isNotNull(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        isNotNull(tableId, column);
        return this;
    }

    public SqlWrapper isNotNull(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        isNotNull(tableId, column);
        return this;
    }

    public <K> SqlWrapper eq(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        eq(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper eq(Integer tableIndex, String column, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        eq(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper eq(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        eq(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper eq(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        eq(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper eq(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        eq(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper eq(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        eq(tableId, column, null, consumer2);
        return this;
    }

    public <K, M> SqlWrapper eq(Integer tableIndex1, SFunction<K, ?> fn1, String tableId2, SFunction<M, ?> fn2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        String tableId1 = tableInfo1.getTableId();
        Field field1 = ColumnUtils.getField(fn1);
        String column1 = getColumn(tableInfo1, field1);
        Field field2 = ColumnUtils.getField(fn2);
        TableInfo tableInfo2 = getTableInfoById(parentTableInfos, tableId2);
        String column2 = getColumn(tableInfo2, field2);
        eq(tableId1, column1, tableId2, column2);
        return this;
    }

    public <K> SqlWrapper eq(Integer tableIndex1, SFunction<K, ?> fn1, String tableId2, String column2) {
        TableInfo tableInfo1 = getTableInfoByIndex(tableIndex1);
        String tableId1 = tableInfo1.getTableId();
        Field field1 = ColumnUtils.getField(fn1);
        String column1 = getColumn(tableInfo1, field1);
        eq(tableId1, column1, tableId2, column2);
        return this;
    }

    public <K> SqlWrapper ne(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        ne(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper ne(Integer tableIndex, String column, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        ne(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper ne(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        ne(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper ne(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        ne(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper ne(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        ne(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper ne(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        ne(tableId, column, null, consumer2);
        return this;
    }

    public <K> SqlWrapper gt(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        gt(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper gt(Integer tableIndex, String column, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        gt(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper gt(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        gt(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper gt(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        gt(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper gt(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        gt(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper gt(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        gt(tableId, column, null, consumer2);
        return this;
    }

    public <K> SqlWrapper ge(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        ge(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper ge(Integer tableIndex, String column, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        ge(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper ge(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        ge(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper ge(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        ge(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper ge(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        ge(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper ge(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        ge(tableId, column, null, consumer2);
        return this;
    }

    public <K> SqlWrapper lt(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        lt(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper lt(Integer tableIndex, String column, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        lt(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper lt(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        lt(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper lt(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        lt(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper lt(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        lt(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper lt(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        lt(tableId, column, null, consumer2);
        return this;
    }

    public <K> SqlWrapper le(Integer tableIndex, SFunction<K, ?> fn, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        le(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper le(Integer tableIndex, String column, Object arg) {
        if (arg == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        le(tableId, column, arg, null);
        return this;
    }

    public SqlWrapper le(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        le(tableId, column, arg, null);
        return this;
    }

    public <K> SqlWrapper le(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        le(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper le(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        le(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper le(Integer tableIndex, Consumer<AggregateWrapper> consumer, Consumer<SqlWrapper> consumer2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        le(tableId, column, null, consumer2);
        return this;
    }

    public <K> SqlWrapper like(Integer tableIndex, SFunction<K, ?> fn, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        like(tableId, column, arg);
        return this;
    }

    public SqlWrapper like(Integer tableIndex, String column, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        like(tableId, column, arg);
        return this;
    }

    public <K> SqlWrapper likeLeft(Integer tableIndex, SFunction<K, ?> fn, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        likeLeft(tableId, column, arg);
        return this;
    }

    public SqlWrapper likeLeft(Integer tableIndex, String column, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        likeLeft(tableId, column, arg);
        return this;
    }

    public <K> SqlWrapper likeRight(Integer tableIndex, SFunction<K, ?> fn, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        likeRight(tableId, column, arg);
        return this;
    }

    public SqlWrapper likeRight(Integer tableIndex, String column, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        likeRight(tableId, column, arg);
        return this;
    }

    public <K> SqlWrapper notLike(Integer tableIndex, SFunction<K, ?> fn, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        notLike(tableId, column, arg);
        return this;
    }

    public SqlWrapper notLike(Integer tableIndex, String column, String arg) {
        if (StringUtils.isBlank(arg)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        notLike(tableId, column, arg);
        return this;
    }

    public <K> SqlWrapper between(Integer tableIndex, SFunction<K, ?> fn, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        between(tableId, column, arg1, arg2);
        return this;
    }

    public SqlWrapper between(Integer tableIndex, String column, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        between(tableId, column, arg1, arg2);
        return this;
    }

    public SqlWrapper between(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        between(tableId, column, arg1, arg2);
        return this;
    }

    public <K> SqlWrapper notBetween(Integer tableIndex, SFunction<K, ?> fn, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        notBetween(tableId, column, arg1, arg2);
        return this;
    }

    public SqlWrapper notBetween(Integer tableIndex, String column, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        notBetween(tableId, column, arg1, arg2);
        return this;
    }

    public SqlWrapper notBetween(Integer tableIndex, Consumer<AggregateWrapper> consumer, Object arg1, Object arg2) {
        if (arg1 == null || arg2 == null) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        AggregateWrapper aggregateWrapper = new AggregateWrapper();
        aggregateWrapper.setTableInfos(tableInfos);
        consumer.accept(aggregateWrapper);
        String column = aggregateWrapper.getColumn();
        notBetween(tableId, column, arg1, arg2);
        return this;
    }

    public <K> SqlWrapper in(Integer tableIndex, SFunction<K, ?> fn, Object... args) {
        if (args == null || args.length == 0) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        List<Object> list = Arrays.asList(args);
        in(tableId, column, list, null);
        return this;
    }

    public SqlWrapper in(Integer tableIndex, String column, Object... args) {
        if (args == null || args.length == 0) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        List<Object> list = Arrays.asList(args);
        in(tableId, column, list, null);
        return this;
    }

    public <K> SqlWrapper in(Integer tableIndex, SFunction<K, ?> fn, List<?> args) {
        if (CollectionUtils.isEmpty(args)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        in(tableId, column, args, null);
        return this;
    }

    public SqlWrapper in(Integer tableIndex, String column, List<?> args) {
        if (CollectionUtils.isEmpty(args)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        in(tableId, column, args, null);
        return this;
    }

    public <K> SqlWrapper in(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        in(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper in(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        in(tableId, column, null, consumer);
        return this;
    }

    public <K> SqlWrapper notIn(Integer tableIndex, SFunction<K, ?> fn, Object... args) {
        if (args == null || args.length == 0) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        List<Object> list = Arrays.asList(args);
        notIn(tableId, column, list, null);
        return this;
    }

    public SqlWrapper notIn(Integer tableIndex, String column, Object... args) {
        if (args == null || args.length == 0) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        List<Object> list = Arrays.asList(args);
        notIn(tableId, column, list, null);
        return this;
    }

    public <K> SqlWrapper notIn(Integer tableIndex, SFunction<K, ?> fn, List<?> args) {
        if (CollectionUtils.isEmpty(args)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        notIn(tableId, column, args, null);
        return this;
    }

    public SqlWrapper notIn(Integer tableIndex, String column, List<?> args) {
        if (CollectionUtils.isEmpty(args)) {
            return this;
        }
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        notIn(tableId, column, args, null);
        return this;
    }

    public <K> SqlWrapper notIn(Integer tableIndex, SFunction<K, ?> fn, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        notIn(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper notIn(Integer tableIndex, String column, Consumer<SqlWrapper> consumer) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        notIn(tableId, column, null, consumer);
        return this;
    }

    public SqlWrapper exists(String sql, @Nullable Object... args) {
        appendExists(sql, args);
        return this;
    }

    public SqlWrapper exists(Consumer<SqlWrapper> consumer) {
        appendExists(consumer);
        return this;
    }

    public SqlWrapper notExists(String sql, @Nullable Object... args) {
        appendNotExists(sql, args);
        return this;
    }

    public SqlWrapper notExists(Consumer<SqlWrapper> consumer) {
        appendNotExists(consumer);
        return this;
    }

    @SafeVarargs
    public final <K> SqlWrapper groupBy(Integer tableIndex, SFunction<K, ?>... fns) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        groupBy(tableInfo, fns);
        return this;
    }

    public final <K, M> SqlWrapper groupBy(Integer tableIndex, SFunction<K, ?> fn1, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        groupBy(tableInfo, fn1, fn2);
        return this;
    }

    public final <K> SqlWrapper groupBy(Integer tableIndex, SFunction<K, ?> fn1, @Nullable String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        groupBy(tableInfo, fn1, beanColumn);
        return this;
    }

    public final <M> SqlWrapper groupBy(Integer tableIndex, String tableColumn, SFunction<M, ?> fn2) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        groupBy(tableInfo, tableColumn, fn2);
        return this;
    }

    public SqlWrapper groupBy(Integer tableIndex, String tableColumn, @Nullable String beanColumn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        groupBy(tableInfo, tableColumn, beanColumn);
        return this;
    }

    public SqlWrapper having(String sql, @Nullable Object... args) {
        appendHaving(sql, args);
        return this;
    }

    public SqlWrapper having(Consumer<SqlWrapper> consumer) {
        appendHaving(consumer);
        return this;
    }

    public <K> SqlWrapper orderBy(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        orderBy(tableId, column, true, false);
        return this;
    }

    public SqlWrapper orderBy(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        orderBy(tableId, column, true, false);
        return this;
    }

    public <K> SqlWrapper orderByDesc(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        orderBy(tableId, column, true, true);
        return this;
    }

    public SqlWrapper orderByDesc(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        orderBy(tableId, column, true, true);
        return this;
    }

    public <K> SqlWrapper thenBy(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        orderBy(tableId, column, false, false);
        return this;
    }

    public SqlWrapper thenBy(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        orderBy(tableId, column, false, false);
        return this;
    }

    public <K> SqlWrapper thenByDesc(Integer tableIndex, SFunction<K, ?> fn) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        Field field = ColumnUtils.getField(fn);
        String column = getColumn(tableInfo, field);
        orderBy(tableId, column, false, true);
        return this;
    }

    public SqlWrapper thenByDesc(Integer tableIndex, String column) {
        TableInfo tableInfo = getTableInfoByIndex(tableIndex);
        String tableId = tableInfo.getTableId();
        orderBy(tableId, column, false, true);
        return this;
    }

    //endregion

    //region 查询器

    public long queryForCount(SqlSession sqlSession) {
        formatFullSql();
        String sqlCount;
        if (CollectionUtils.isEmpty(columnInfos)) {
            sqlCount = String.format(sqlBuilder.toString(), "count(*) selectCount");
        }
        else {
            sqlCount = String.format("select count(*) selectCount from (%s) t", sql);
        }
        log.info(sqlCount);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        Map<String, Object> map;
        try {
            map = sqlRunner.selectOne(sqlCount, args);
            return (long) map.get("selectCount");
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        return 0;
    }

    public <T> T queryForObject(Class<T> clazz, SqlSession sqlSession) {
        formatFullSql();
        sql += orderBy.toString();
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        if (CollectionUtils.isEmpty(mapList)) {
            return null;
        }
        Map<String, Object> map = mapList.stream().findFirst().orElse(null);
        if (map == null) {
            return null;
        }
        updateMap(map, clazz);
        return mapToBean(map, clazz);
    }

    public Map<String, Object> queryForMap(SqlSession sqlSession) {
        formatFullSql();
        sql += orderBy.toString();
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        if (CollectionUtils.isEmpty(mapList)) {
            return null;
        }
        return mapList.stream().findFirst().orElse(null);
    }

    public <T> List<T> queryForObjects(Class<T> clazz, SqlSession sqlSession) {
        formatFullSql();
        sql += orderBy.toString();
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        if (CollectionUtils.isEmpty(mapList)) {
            return null;
        }
        updateMapList(mapList, clazz);
        return mapsToBeans(mapList, clazz);
    }

    public List<Map<String, Object>> queryForMaps(SqlSession sqlSession) {
        formatFullSql();
        sql += orderBy.toString();
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        return mapList;
    }

    public <T> Page<T> queryForObjectPage(Class<T> clazz, SqlSession sqlSession, int pageIndex, int pageSize) {
        formatFullSql();
        int total = (int) queryForCount(sqlSession);
        sql += orderBy.toString();
        int pages = total % pageSize > 0 ? (total / pageSize) + 1 : total / pageSize;
        Page<T> page = new Page<>();
        page.setTotal(total).setPages(pages).setCurrent(pageIndex).setSize(pageSize);
        sql += String.format(" LIMIT %d,%d", (pageIndex - 1) * pageSize, pageSize);
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        if (CollectionUtils.isEmpty(mapList)) {
            return page;
        }
        updateMapList(mapList, clazz);
        List<T> list = mapsToBeans(mapList, clazz);
        page.setRecords(list);
        return page;
    }

    public Page<Map<String, Object>> queryForMapPage(SqlSession sqlSession, int pageIndex, int pageSize) {
        formatFullSql();
        int total = (int) queryForCount(sqlSession);
        sql += orderBy.toString();
        int pages = total % pageSize > 0 ? (total / pageSize) + 1 : total / pageSize;
        Page<Map<String, Object>> page = new Page<>();
        page.setTotal(total).setPages(pages).setCurrent(pageIndex).setSize(pageSize);
        sql += String.format(" LIMIT %d,%d", (pageIndex - 1) * pageSize, pageSize);
        log.info(sql);
        SqlRunner sqlRunner = new SqlRunner(sqlSession.getConnection());
        List<Map<String, Object>> mapList = null;
        try {
            mapList = sqlRunner.selectAll(sql, args);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
        page.setRecords(mapList);
        return page;
    }

    //endregion

}
