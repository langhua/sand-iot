<script setup lang="ts">
import axios from 'axios'
import { useRoute } from 'vue-router'
import LocaleSwitcher from "@/components/LocaleSwitcher.vue"
const env = import.meta.env
const route = useRoute()
const details = await axios({
                              method: 'POST',
                              headers: { 'content-type': 'application/x-www-form-urlencoded' },
                              data: 'type=' + route.params.type + '&base64Name=' + route.params.base64Name,
                              url: env.VITE_SANDFLOWER_SERVER + env.VITE_SANDFLOWER_MDNSDETAILS_API,
                              timeout: 10000,
                              })
    .then(response => {
      if (response.data != null && response.data.serviceInfo != null) {
        console.log(response.data.serviceInfo)
        return JSON.parse(response.data.serviceInfo)
      }
    })
</script>

<template>
  <main>
  <nav class="home">
    <RouterLink :to="env.VITE_MDNS_BASE_URL">{{ $t('mdns.home') }}</RouterLink>
  </nav>
  <div class="locale">
    <LocaleSwitcher/>
  </div>
  <div v-if="details != null" class="list" ref="mdnslist">
    <div class="list-row">
      <div class="list-column">{{ $t('mdns.attribute') }}</div>
      <div class="list-column">{{ $t('mdns.value') }}</div>
    </div>
    <div class="list-row" v-for="(value, attribute) in details" :key='attribute'>
      <div class="list-item">{{ $t('service.' + attribute) ? $t('service.' + attribute) : attribute }}</div>
      <div class="list-item">
        <div v-if="typeof value === 'object'">
          <div v-for="(property, name) in value" :key="name">
            <div v-if="(typeof name === 'number')">{{ property }}</div>
            <div v-else>{{ name }}: {{ property }}</div>
          </div>
        </div>
        <div v-else>{{ value }}</div>
      </div>
    </div>
  </div>
  <div v-else>
    No data returned.
  </div>
  </main>
</template>

<style>
.details {
  align-items: baseline;
  margin-top: 10px;
}

@media (min-width: 1024px) {
  .details {
    min-height: 100vh;
    display: flex;
    margin-top: 150px;
  }
}

.list {
  width: 100%;
}

.list-row {
   border-bottom: 1px groove var(--color-border);
}

.list-column {
  width: 49%;
  display: inline-block;
  font-weight: 500;
  font-size: 0.8rem;
}

div.list-item {
  width: 49%;
  display: inline-block;
  margin: 1px;
  vertical-align: middle;
  word-wrap: break-word;
  font-size: 0.7rem;
}

nav.list-item {
  margin: 0px;
  padding: 0px;
}
</style>
