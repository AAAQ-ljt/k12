import { Select, Table } from 'antd';
import type { TableProps, TablePaginationConfig } from 'antd';

/**
 * 自定义每页条数选择器：渲染到 body，避免被 layout-content 的 overflow 裁剪
 */
function PageSizeChanger(props: {
  value?: number;
  onChange?: (value: number) => void;
  disabled?: boolean;
  className?: string;
}) {
  const { value, onChange, disabled, className } = props;
  return (
    <Select
      value={value}
      onChange={(v) => onChange?.(v as number)}
      disabled={disabled}
      className={className}
      showSearch={false}
      style={{ width: 100 }}
      options={[10, 15, 20, 50].map((n) => ({ label: `${n} 条/页`, value: n }))}
      getPopupContainer={() => document.body}
    />
  );
}

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
          showSizeChanger: { showSearch: false },
          showQuickJumper: true,
          pageSizeOptions: [10, 15, 20, 50],
          showTotal: (total: number) => `共 ${total} 条`,
          components: { sizeChanger: PageSizeChanger },
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
