import BaseDialog from '@/components/BaseDialog';
import {
  getFilePreviewUrl,
  getStudentFilePreviewUrl,
  type ResourceInfo,
} from '@/api/resource';
import styles from './DocumentPreviewModal.module.scss';

interface DocumentPreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  userId?: string;
  onClose: () => void;
}

export default function DocumentPreviewModal({
  open,
  resource,
  onClose,
  userId,
}: DocumentPreviewModalProps) {
  const previewUrl = resource
    ? userId
      ? getStudentFilePreviewUrl(resource.resourceId, userId)
      : getFilePreviewUrl(resource.resourceId)
    : '';

  return (
    <BaseDialog
      className={styles.documentPreviewModal}
      open={open}
      title={resource?.resourceName || '文档预览'}
      width="85vw"
      top={32}
      showCancel={false}
      footer={null}
      contentPadding={0}
      bodyStyle={{ padding: 0, maxHeight: 'none', overflow: 'hidden' }}
      onCancel={onClose}
    >
      <div className={styles.previewFrame}>
        {resource && (
          <iframe
            key={resource.resourceId}
            src={previewUrl}
            title={resource.resourceName}
          />
        )}
      </div>
    </BaseDialog>
  );
}
