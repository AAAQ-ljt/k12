/**
 * 删除 / 移除若干条数据后，计算列表应请求的页码。
 *
 * 场景：在第 2 页及以后删掉本页最后一条时，总数会落到上一页的数据范围内，
 * 继续用原页码请求就会得到空列表（看起来像整页数据被清空）。
 *
 * 判定：剩余条数 <= 当前页之前的所有条数（即 (pageNo - 1) * pageSize）时，
 * 说明当前页已越界，回退一页；否则停留在原页。
 *
 * @param pageNo       当前页码（从 1 开始）
 * @param pageSize     每页条数
 * @param totalCount   删除前的总条数（刷新前列表持有的值）
 * @param removedCount 本次删除的条数，默认 1
 */
export function resolvePageNoAfterRemove(
  pageNo: number,
  pageSize: number,
  totalCount: number,
  removedCount = 1,
): number {
  if (pageNo <= 1) {
    return pageNo;
  }
  const remaining = totalCount - removedCount;
  if (remaining <= (pageNo - 1) * pageSize) {
    return pageNo - 1;
  }
  return pageNo;
}
