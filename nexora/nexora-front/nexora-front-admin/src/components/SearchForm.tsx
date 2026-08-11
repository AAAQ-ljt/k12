import { Form, Button } from 'antd';
import type { ReactNode } from 'react';
import { Search, RotateCcw } from 'lucide-react';
import styles from '@/assets/styles/searchForm.module.scss';

interface SearchFormProps {
  children: ReactNode;
  onSearch: () => void;
  onReset: () => void;
}

export default function SearchForm({ children, onSearch, onReset }: SearchFormProps) {
  return (
    <Form layout="inline" className={styles.searchForm}>
      {children}
      <Form.Item>
        <div className={styles.searchButtons}>
          <Button type="primary" icon={<Search size={14} />} onClick={onSearch}>
            查询
          </Button>
          <Button icon={<RotateCcw size={14} />} onClick={onReset}>
            重置
          </Button>
        </div>
      </Form.Item>
    </Form>
  );
}
