import { Image } from 'antd';
import BaseDialog from '@/components/BaseDialog';
import { getImagePreviewUrl, type ResourceInfo } from '@/api/resource';
import styles from './ImagePreviewModal.module.scss';

interface ImagePreviewModalProps {
  open: boolean;
  resource: ResourceInfo | null;
  onClose: () => void;
}

export default function ImagePreviewModal({ open, resource, onClose }: ImagePreviewModalProps) {
  return (
    <BaseDialog
      className={styles.imagePreviewModal}
      open={open}
      title={resource?.resourceName || '图片预览'}
      width="72vw"
      top={40}
      showCancel={false}
      footer={null}
      contentPadding={0}
      bodyStyle={{ padding: 0, maxHeight: 'none', overflow: 'hidden' }}
      onCancel={onClose}
    >
      <div className={styles.previewBody}>
        {resource && (
          <Image
            src={getImagePreviewUrl(resource.resourceId)}
            alt={resource.resourceName}
            className={styles.previewImage}
          />
        )}
      </div>
    </BaseDialog>
  );
}
