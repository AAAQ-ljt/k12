import React, { useState } from 'react';
import { Table, Pagination } from 'antd';
import styles from './BaseDataTable.module.scss';

/**
 * 分页数据结构
 */
export interface PageResult<T = unknown> {
  totalCount: number;
  pageSize: number;
  pageNo: number;
  pageTotal: number;
  list: T[];
}

/**
 * 表格列定义
 */
export interface TableColumn<T> {
  title?: string;
  key?: string;
  dataIndex?: string;
  width?: number | string;
  align?: 'left' | 'center' | 'right';
  fixed?: 'left' | 'right' | false;
  render?: (value: unknown, record: T, index: number) => React.ReactNode;
  ellipsis?: boolean;
  className?: string;
}

export interface BaseDataTableProps<T extends Record<string, any>> {
  /** 数据源（包含分页信息）*/
  data: PageResult<T>;
  /** 表格列定义 */
  columns: TableColumn<T>[];
  /** 标题 */
  title?: string;
  /** 是否显示边框，默认 false*/
  bordered?: boolean;
  /** 复选框是否启用 */
  selectable?: boolean;
  /** 已选中的行键列表 */
  selectedRowKeys?: string[] | number[];
  /** 选中行变更回调 */
  onSelectChange?: (selectedRowKeys: string[] | number[], selectedRecords: T[]) => void;
  /** 加载状态 */
  loading?: boolean;
  /** 分页页码变更回调 */
  onPaginationChange?: (pageNo: number, pageSize: number) => void;
  /** rowKey 字段名或函数 */
  rowKeyExtractor?: string | ((record: T) => string | number);
  /** 自定义插槽内容（通过 render 函数传入）*/
  children?: React.ReactNode;
}

const DEFAULT_PAGE_SIZE = 15;

/**
 * 通用分页表格组件
 * - 支持分页、复选、自定义渲染、自适应高度、内联滚动
 */
export default function BaseDataTable<T extends Record<string, any>>(
  props: BaseDataTableProps<T>,
) {
  const {
    data,
    columns,
    title,
    bordered = false,
    selectable = false,
    selectedRowKeys = [],
    onSelectChange,
    loading = false,
    onPaginationChange,
    rowKeyExtractor,
  } = props;

  const [pagination] = useState({
    current: data.pageNo || 1,
    pageSize: data.pageSize || DEFAULT_PAGE_SIZE,
    total: data.totalCount,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  // 处理分页变更
  const handlePaginationChange = (
    current: number,
    pageSize: number,
  ) => {
    onPaginationChange?.(current, pageSize);
  };

  // 处理选择行变更
  const handleSelectChange = (
    selectedRowKeys: string[] | number[],
    selectedRecords: T[],
  ) => {
    onSelectChange?.(selectedRowKeys, selectedRecords);
  };

  // 构建 column 数组
  const buildColumns = () => {
    return columns.map(col => {
      const antdCol = {
        title: col.title,
        dataIndex: col.dataIndex,
        key: col.key || col.dataIndex,
        width: col.width,
        align: col.align,
        fixed: col.fixed,
        ellipsis: col.ellipsis || false,
        className: col.className,
      };

      // 如果提供了 render 函数，直接使用
      if (col.render) {
        (antdCol as any).render = col.render;
      }

      return antdCol;
    });
  };

  // 确定 rowKey
  const getRowKey = (): string | ((record: T) => string | number) => {
    if (typeof rowKeyExtractor === 'string') {
      return rowKeyExtractor;
    } else if (rowKeyExtractor) {
      return rowKeyExtractor;
    } else if (columns.some(c => c.key)) {
      const keyColumn = columns.find(c => c.key);
      return keyColumn!.key as string;
    } else {
      return 'id'; // 默认使用 id
    }
  };

  // 复选框配置
  const rowSelection = selectable
    ? {
        selectedRowKeys,
        onChange: handleSelectChange,
      }
    : undefined;

  return (
    <div className={styles.container}>
      {/* 标题 */}
      {title && (
        <div className={styles.header}>
          <h3 className={styles.title}>{title}</h3>
        </div>
      )}

      {/* 表格容器 - 撑满可见区域 */}
      <div className={styles.tableWrapper}>
        <div className={styles.tableContent}>
          <Table
            dataSource={data.list}
            columns={buildColumns()}
            rowKey={getRowKey()}
            pagination={false} // 使用自定义分页
            scroll={{ x: 'max-content', y: 'auto' }} // 横向和纵向滚动
            bordered={bordered}
            loading={loading}
            rowSelection={rowSelection}
            size="middle" // 适中大小
            style={{ margin: 0 }}
          />
        </div>
      </div>

      {/* 自定义分页 */}
      <div className={styles.pagination}>
        <Pagination
          current={pagination.current}
          pageSize={pagination.pageSize}
          total={pagination.total}
          showSizeChanger={pagination.showSizeChanger}
          showQuickJumper={pagination.showQuickJumper}
          showTotal={pagination.showTotal}
          onChange={handlePaginationChange}
          className={styles.paginationComponent}
        />
      </div>
    </div>
  );
}
