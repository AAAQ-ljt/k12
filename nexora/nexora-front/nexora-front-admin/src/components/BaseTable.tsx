import { Table } from 'antd';
import type { TableProps, TablePaginationConfig } from 'antd';

export type PaginationConfig = TablePaginationConfig;

interface BaseTableProps<T> {
  columns: TableProps<T>['columns'];
  dataSource: T[];
  loading?: boolean;
  pagination?: false | TablePaginationConfig;
  onChange?: TableProps<T>['onChange'];
  rowKey: string | ((record: T) => string | number);
  rowSelection?: TableProps<T>['rowSelection'];
}

export default function BaseTable<T extends Record<string, any>>(
  props: BaseTableProps<T>,
) {
  const { columns, dataSource, loading, pagination, onChange, rowKey, rowSelection } = props;

  const mergedPagination: false | TablePaginationConfig =
    pagination === false
      ? false
      : {
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
          ...(pagination ?? {}),
        };

  return (
    <Table<T>
      columns={columns}
      dataSource={dataSource}
      loading={loading}
      pagination={mergedPagination}
      onChange={onChange}
      rowKey={rowKey}
      rowSelection={rowSelection}
    />
  );
}
