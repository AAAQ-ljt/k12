package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.entity.enums.CommentStatusEnum;
import com.easymall.entity.enums.ResponseCodeEnum;
import com.easymall.entity.po.OrderComment;
import com.easymall.entity.query.OrderCommentQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.exception.BusinessException;
import com.easymall.service.OrderCommentService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order/comment")
@Validated
public class OrderCommentController extends ABaseController {

    @Resource
    private OrderCommentService orderCommentService;

    @RequestMapping("/postComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postComment(@NotEmpty String orderId, @NotEmpty @Size(max = 300) String commentContent, @Size(max = 300) String commentImages,
                                  @NotNull @Max(5) @Min(1) Integer star) {
        orderCommentService.postComment(getTokenUserInfo().getUserId(), orderId, commentContent, commentImages, star);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/postReComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO postReComment(@NotEmpty String orderId,
                                    @NotEmpty @Size(max = 300) String reCommentContent,
                                    @Size(max = 300) String reCommentImages) {
        orderCommentService.postReComment(getTokenUserInfo().getUserId(), orderId, reCommentContent, reCommentImages);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getComment(@NotEmpty String orderId) {
        OrderComment orderComment = orderCommentService.getOrderCommentByOrderId(orderId);
        if (!orderComment.getUserId().equals(getTokenUserInfo().getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return getSuccessResponseVO(orderComment);
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(@NotEmpty String productId, Integer pageNo) {
        OrderCommentQuery commentQuery = new OrderCommentQuery();
        commentQuery.setProductId(productId);
        commentQuery.setOrderBy("comment_time desc");
        commentQuery.setPageNo(pageNo);
        commentQuery.setStatus(CommentStatusEnum.NORMAL.getStatus());
        commentQuery.setQueryUserInfo(true);
        PaginationResultVO resultVO = this.orderCommentService.findListByPage(commentQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/loadMyComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadMyComment(Integer pageNo) {
        OrderCommentQuery query = new OrderCommentQuery();
        query.setOrderBy("o.comment_time desc");
        query.setQueryProduct(true);
        query.setUserId(getTokenUserInfo().getUserId());
        query.setPageNo(pageNo);
        query.setStatus(CommentStatusEnum.NORMAL.getStatus());
        return getSuccessResponseVO(orderCommentService.findListByPage(query));
    }

    @RequestMapping("/delMyComment")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delMyComment(String orderId) {
        OrderComment orderComment = new OrderComment();
        orderComment.setStatus(CommentStatusEnum.DEL.getStatus());

        OrderCommentQuery orderCommentQuery = new OrderCommentQuery();
        orderCommentQuery.setOrderId(orderId);
        orderCommentQuery.setUserId(getTokenUserInfo().getUserId());
        orderCommentService.updateByParam(orderComment, orderCommentQuery);
        return getSuccessResponseVO(null);
    }
}
