<script setup lang="ts">
import ServiceTypes from '@/views/ServiceTypes.vue';
import LocaleSwitcher from "@/components/LocaleSwitcher.vue"
const env = import.meta.env
</script>

<template>
  <main>
    <nav class="home">
      <RouterLink :to="env.VITE_MDNS_BASE_URL">{{ $t('mdns.home') }}</RouterLink>
    </nav>
    <div class="locale">
      <LocaleSwitcher/>
    </div>
    <ServiceTypes/>
  </main>
</template>
