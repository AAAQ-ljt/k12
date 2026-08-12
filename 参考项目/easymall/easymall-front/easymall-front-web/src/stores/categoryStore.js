import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCategoryStore = defineStore('categoryStore', () => {

    const categoryList = ref([]);

    const setCategoryList = (data) => {
        categoryList.value = data
    }

    return { categoryList, setCategoryList }
})
