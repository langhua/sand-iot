<script lang="ts">
export default {
  name: "LocaleSwitcher",
  data() {
    return { locales: ["zh", "en"] };
  }
}
</script>

<template>
  <select v-model="$i18n.locale" name="locale">
    <option v-for="(locale, i) in locales" :key="`locale-${i}`" :value="locale">
      {{ $t('locale.' + locale) }}
    </option>
  </select>
</template>

<style>
select {
  font-size: 0.7em;
}
</style>