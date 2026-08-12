import { Image, Avatar } from 'antd';
import type { CSSProperties } from 'react';

interface CoverImageProps {
  /** 图片地址 */
  src?: string;
  /** 图片宽度（默认 100%） */
  width?: number | string;
  /** 圆角大小（默认 8） */
  radius?: number;
  /** 宽高比，如 16/9、1（默认 16/9） */
  aspectRatio?: number;
  /** 头像模式（圆形，自动忽略 aspectRatio） */
  avatar?: boolean;
  /** 无图片时的占位文本/图标 */
  placeholder?: React.ReactNode;
  /** 图片说明 */
  alt?: string;
}

/**
 * 封面组件：支持课程封面、个人头像等
 * 可自定义宽度、圆角大小、宽高比；无图时展示占位
 */
export default function CoverImage({
  src,
  width = '100%',
  radius = 8,
  aspectRatio = 16 / 9,
  avatar = false,
  placeholder,
  alt = '',
}: CoverImageProps) {
  const style: CSSProperties = avatar
    ? {}
    : {
        width,
        aspectRatio: `${aspectRatio}`,
        borderRadius: radius,
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f5f5f5',
      };

  if (avatar) {
    return <Avatar src={src} size={Number(width) || 40} style={{ borderRadius: radius }}>{placeholder}</Avatar>;
  }

  if (!src) {
    return <div style={style}>{placeholder}</div>;
  }

  return (
    <div style={style}>
      <Image
        src={src}
        alt={alt}
        width="100%"
        height="100%"
        style={{ objectFit: 'cover', borderRadius: radius }}
        fallback="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHJlY3Qgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiIGZpbGw9IiNmNWY1ZjUiLz48dGV4dCB4PSI1MCIgeT0iNTUiIGZvbnQtc2l6ZT0iMTIiIGZpbGw9IiNiZmJmYmYiIHRleHQtYW5jaG9yPSJtaWRkbGUiPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg=="
        preview={false}
      />
    </div>
  );
}
