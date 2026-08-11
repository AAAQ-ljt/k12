import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import pinia from '@/stores'
import 'element-plus/dist/index.css'
import '@/assets/styles/base.scss'

const app = createApp(App)
app.use(ElementPlus);
app.use(pinia)
app.use(router)

app.mount('#app')
