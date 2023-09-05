import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import ServiceDetails from '@/views/ServiceDetails.vue'
const env = import.meta.env

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: env.VITE_MDNS_BASE_URL,
      name: 'home',
      component: HomeView
    },
    {
      path: env.VITE_MDNS_BASE_URL + 'details/:type/:base64Name',
      name: 'details',
      component: ServiceDetails
    }
  ]
})

export default router
