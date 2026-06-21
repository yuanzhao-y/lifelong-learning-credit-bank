import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDictItemsByCode } from '@/api/system'

export const useDictStore = defineStore('dict', () => {
  const cache = ref<Map<string, any[]>>(new Map())

  async function loadDict(dictCode: string): Promise<any[]> {
    if (cache.value.has(dictCode)) return cache.value.get(dictCode)!
    try {
      const items = await getDictItemsByCode(dictCode)
      cache.value.set(dictCode, items as any)
      return items as any
    } catch {
      return []
    }
  }

  function clearCache(dictCode?: string) {
    if (dictCode) cache.value.delete(dictCode)
    else cache.value.clear()
  }

  return { cache, loadDict, clearCache }
})
