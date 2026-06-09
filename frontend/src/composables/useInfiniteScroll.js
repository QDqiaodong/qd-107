import { ref, computed } from 'vue'

export function useInfiniteScroll(fetchFn, options = {}) {
  const { pageSize = 10, immediate = true } = options

  const list = ref([])
  const page = ref(1)
  const pageSizeRef = ref(pageSize)
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)
  const finished = ref(false)

  const hasMore = computed(() => !finished.value && !loading.value)

  const loadMore = async () => {
    if (loading.value || finished.value) return

    loading.value = true
    error.value = null

    try {
      const result = await fetchFn(page.value, pageSizeRef.value)
      const items = result.list || result.data || result.items || []
      const totalCount = result.total ?? result.count ?? items.length

      list.value = [...list.value, ...items]
      total.value = totalCount

      if (items.length < pageSizeRef.value || list.value.length >= totalCount) {
        finished.value = true
      } else {
        page.value++
      }
    } catch (err) {
      error.value = err.message || '加载失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const retry = () => {
    error.value = null
    return loadMore()
  }

  const reset = () => {
    list.value = []
    page.value = 1
    finished.value = false
    error.value = null
    loading.value = false
  }

  const refresh = async () => {
    reset()
    await loadMore()
  }

  if (immediate) {
    loadMore()
  }

  return {
    list,
    page,
    pageSize: pageSizeRef,
    total,
    loading,
    error,
    finished,
    hasMore,
    loadMore,
    retry,
    reset,
    refresh
  }
}
