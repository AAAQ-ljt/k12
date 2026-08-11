import Request from "@/utils/Request"
const Api = {
    //登录 退出
    checkCode: "/account/checkCode",
    login: "/account/login",//登录
    logout: "/account/logout",//退出
    //资源
    sourcePath: "/api/file/getResource?sourceName=",
    uploadImage: "/file/uploadImage",
    //分类
    loadCategory: "/sysCategory/loadCategory",//获取分类
    saveCategory: "/sysCategory/saveCategory",//保存分类
    delCategory: "/sysCategory/delCategory",//删除分类
    changeCategorySort: "/sysCategory/changeCategorySort",//修改分类排序
    //商品属性
    saveProductProperty: "/sysCategory/saveProductProperty",//保存商品属性
    delProductProperty: "/sysCategory/delProductProperty",//删除商品属性
    //商品
    addProduct: "/productInfo/addProduct",//新增商品
    updateProduct: "/productInfo/updateProduct",//修改商品
    getProductInfo: "/productInfo/getProductInfo",//获取商品详情
    loadProduct: "/productInfo/loadProduct",//获取商品列表
    updateSkuStock: "/productInfo/updateSkuStock",//更新库存
    updateProductStatus: "/productInfo/updateProductStatus",//修改状态
    deleteProduct: "/productInfo/deleteProduct",//删除商品
    commendProduct: "/productInfo/commendProduct",//推荐商品
    //订单
    loadOrderStatus: "/order/loadOrderStatus",//订单状态
    loadOrder: "/order/loadOrder",//订单列表
    getLogistics: "/order/getLogistics",//获取物流信息
    delivery: "/order/delivery",//发货
    getComment: "/order/getComment",//获取评论
    bizComment: "/order/bizComment",//商家评论
    //评论
    loadComment: "/order/loadComment",//评论
    delComment: "/order/delComment",//删除评论
    //用户列表
    loadUser: "/user/loadUser",//获取用户信息,
    changeStatus: "/user/changeStatus",//启用禁用用户
    //系统设置
    saveSysSaveLogistics: "/setting/saveLogistics",//保存系统发货地址
    getSysLogistics: "/setting/getLogistics",//获取发货地址
    loadPromptList: "/setting/loadPromptList",//获取提示词列表
    getPromptDetail: "/setting/getPromptDetail",//获取提示词详情
    savePrompt: "/setting/savePrompt",//保存提示词
    cleanPromptCache: "/setting/cleanPromptCache",//清空提示词
    //RAG
    loadRagQuestion: "/rag/loadRagQuestion",//分页查询
    saveRagQuestion: "/rag/saveRagQuestion",//保存rag
    delRagQuestion: "/rag/delRagQuestion",//删除rag
    //首页
    getTodayData: "/home/getTodayData",
    loadWeeklyStatisticsData: "/home/loadWeeklyStatisticsData",
    loadLessStockProduct: "/home/loadLessStockProduct",

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