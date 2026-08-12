import "@/assets/icon/iconfont.css"
import '@/assets/base.scss';

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import Request from "@/utils/Request"
import Message from "@/utils/Message"
import Utils from "@/utils/Utils"
import { Confirm, Alert } from "@/utils/Confirm.js"
import { Api } from "@/utils/Api.js"
import Verify from "@/utils/Verify.js"

import Dialog from "@/components/Dialog.vue";
import Drawer from "@/components/Drawer.vue";
import Table from "@/components/Table.vue";
import ImageSelect from "@/components/ImageSelect.vue";
import Cover from "@/components/Cover.vue";
import Avatar from "@/components/Avatar.vue";
import OpBtn from "@/components/OpBtn.vue";
import Price from "@/components/Price.vue";

const app = createApp(App)
app.use(ElementPlus);
app.use(createPinia())
app.use(router)


app.component('ImageSelect', ImageSelect)
app.component('Cover', Cover)
app.component("Avatar", Avatar);
app.component('Dialog', Dialog)
app.component('Drawer', Drawer)
app.component('Table', Table)
app.component('OpBtn', OpBtn)
app.component("Price", Price);


app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Utils = Utils;
app.config.globalProperties.Api = Api
app.config.globalProperties.Confirm = Confirm;
app.config.globalProperties.Alert = Alert;
app.config.globalProperties.Verify = Verify;
//商品主图数量
app.config.globalProperties.productMainImageCount = 5;
app.config.globalProperties.imageThumbnailSuffix = "_thumbnail"
//图片后缀
app.config.globalProperties.imageAccept = ".jpg,.png,.gif,.bmp,.webp";
app.mount('#app')
