import { createI18n } from 'vue-i18n';
import en from './en'
import zh from './zh';

export const i18n = createI18n({
  locale: localStorage.getItem('lang') || 'zh',
  fallbackLocale: 'zh',
  legacy: false, // Composition API 模式
  globalInjection: true, // 全局注册 $t方法
  messages: {
    en: en,
    zh: zh
  }
})
