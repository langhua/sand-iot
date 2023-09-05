<script setup lang="ts">
import axios from 'axios'

const env = import.meta.env
const types = await axios.post(env.VITE_SANDFLOWER_SERVER + env.VITE_SANDFLOWER_MDNSTYPES_API)
                         .then(response => {
                                            console.log(JSON.parse(response.data.mdnsTypes))
                                            return JSON.parse(response.data.mdnsTypes)
                                          })
</script>

<template>
  <div class="list">
    <div class="list-row">
      <div class="list-column">{{ $t('mdns.types') }}</div>
      <div class="list-column">{{ $t('mdns.services') }}</div>
    </div>
    <div class="list-row" v-for="(services, type) in types" :key='type'>
      <div class="list-item">{{ type }}</div>
      <div class="list-item">
        <nav class="list-item" v-for="(attrs, service) in services" :key="service">
          <RouterLink v-if="attrs.status == 'true'" :to="{name: 'details', params: {'type': type, 'base64Name': attrs.base64Name}}">
          {{ service }}
          </RouterLink>
          <div v-else>{{ service }}</div>
        </nav>
      </div>
    </div>
  </div>
  </template>

<style scoped>
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
