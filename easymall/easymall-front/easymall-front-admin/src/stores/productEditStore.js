import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useProductEditStore = defineStore('productEditStore', () => {
    const productPropertyList = ref([]);
    const skuList = ref([]);
    const skuData = ref(new Map())
    return { productPropertyList, skuList, skuData }
})
