// NestStay-Shell consumer detail redirect guard
// Injected into router.js before export default router

const SHELL_CONTACT_URL = '__SHELL_CONTACT_URL__'
const shellDetailPaths = new Set([
  '/index/homestayInfoDetail',
  '/index/forumDetail',
  '/index/newsDetail',
  '/index/platformViewDetail',
  '/index/news-detail'
])

router.beforeEach((to, from, next) => {
  if (shellDetailPaths.has(to.path)) {
    window.location.href = SHELL_CONTACT_URL
    return
  }
  next()
})
