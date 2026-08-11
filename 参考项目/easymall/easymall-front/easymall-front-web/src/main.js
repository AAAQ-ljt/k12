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

import Cover from "@/components/Cover.vue"
import Avatar from "@/components/Avatar.vue"
import DataLoadMoreList from "@/components/DataLoadMoreList.vue"
import NoData from "@/components/NoData.vue"
import Price from "@/components/Price.vue"
import Drawer from "@/components/Drawer.vue"
import Dialog from "@/components/Dialog.vue"

const app = createApp(App)
app.use(ElementPlus);
app.use(createPinia())
app.use(router)

app.component("Drawer", Drawer);
app.component("Dialog", Dialog);
app.component("Cover", Cover);
app.component("Avatar", Avatar);
app.component("NoData", NoData);
app.component("Price", Price);
app.component("DataLoadMoreList", DataLoadMoreList);

app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Utils = Utils;
app.config.globalProperties.Api = Api
app.config.globalProperties.Confirm = Confirm;
app.config.globalProperties.Alert = Alert;
app.config.globalProperties.Verify = Verify;
app.config.globalProperties.bodyWidth = 1300
app.config.globalProperties.imageThumbnailSuffix = "_thumbnail"
app.config.globalProperties.imageAccept = ".jpg,.png,.gif,.bmp,.webp";
app.mount('#app')
