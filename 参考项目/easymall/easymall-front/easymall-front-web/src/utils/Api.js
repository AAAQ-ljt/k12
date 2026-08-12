import Request from "./Request";
//单服务版本
const Api = {
    sourcePath: "/api/file/getResource?sourceName=",
    uploadImage: "/file/uploadImage",
    checkCode: "/account/checkCode",//验证码
    login: "/account/login",//登录
    logout: "/account/logout",//退出登录
    register: "/account/register",//注册
    autoLogin: "/account/autoLogin",//自动登录
    updatePassword: "/account/updatePassword",//修改密码
    getUserInfo: "/account/getUserInfo",//获取个人信息
    updateUserInfo: "/account/updateUserInfo",//修改个人信息
    //商品信息
    loadCategory: "/product/loadCategory",//获取分类
    loadCommendProduct: "/product/loadCommendProduct",//获取推荐商品
    loadProduct: "/product/loadProduct",//获取商品列表
    getProduct: "/product/getProduct",//获取商品信息,
    search: "/product/search",//搜索
    //地址
    loadUserAddress: "/userAddress/loadDataList",//获取用户地址
    addAddress: "/userAddress/addAddress",//新增地址
    updateAddress: "/userAddress/updateAddress",//修改地址
    delAddress: "/userAddress/delAddress",//删除地址
    updateDefault: "/userAddress/updateDefault",//设为默认
    //购物车
    loadProductCart: "/productCart/loadProductCart",//购物车列表
    add2Cart: "/productCart/add2Cart",//加入购物车
    deleteCart: "/productCart/deleteCart",//移除购物车
    //订单
    postOrder: "/order/postOrder",//提交订单
    getOrderInfo: "/order/getOrderInfo",//获取订单详情
    loadMyOrder: "/order/loadMyOrder",//查询我的订单
    getPayInfo: "/order/getPayInfo",//获取支付信息
    cancelOrder: "/order/cancelOrder",//取消订单
    deleteOrder: "/order/deleteOrder",//删除订单
    confirmOrder: "/order/confirmOrder",//确认下单
    refundOrder: "/order/refundOrder",//退款
    getLogistics: "/order/getLogistics",//物流信息
    postComment: "/order/comment/postComment",//评价
    getComment: "/order/comment/getComment",//获取评价
    postReComment: "/order/comment/postReComment",//追评
    loadComment: "/order/comment/loadComment",//获取商品所有评论,
    loadMyComment: "/order/comment/loadMyComment",//获取我的评论,
    delMyComment: "/order/comment/delMyComment",//删除评论
    getOrderCountInfo: "/order/getOrderCountInfo",//订单数量
    //agent
    sendMessage: "/agent/sendMessage",//发送消息
    cancelMessage: "/agent/cancelMessage",//取消消息
    loadHistoryMessage: "/agent/loadHistoryMessage",//获取历史消息
}
const uploadImage = async (file, createThumbnail = false) => {
    let result = await Request({
        url: Api.uploadImage,
        params: {
            file,
            createThumbnail
        },
    })
    if (!result) {
        return;
    }
    return result.data;
}
export {
    Api,
    uploadImage
}