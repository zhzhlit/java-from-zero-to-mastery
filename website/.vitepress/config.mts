import { defineConfig } from 'vitepress'
import { fileURLToPath } from 'node:url'

const vuePackages = new Set(['vue', 'vue/server-renderer'])

export default defineConfig({
  title: 'Java 从零到精通',
  description: '以 Java 21 为基线的系统化 Java 学习项目',
  base: '/java-from-zero-to-mastery/',
  srcDir: '../docs',
  outDir: './.vitepress/dist',
  rewrites: {
    'en/README.md': 'en/index.md'
  },
  vite: {
    plugins: [
      {
        name: 'resolve-vitepress-vue',
        enforce: 'pre',
        resolveId(source) {
          return vuePackages.has(source)
            ? fileURLToPath(import.meta.resolve(source))
            : null
        }
      }
    ]
  },
  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-CN',
      link: '/'
    },
    en: {
      label: 'English',
      lang: 'en',
      link: '/en/'
    }
  },
  themeConfig: {
    i18nRouting: false,
    search: {
      provider: 'local'
    },
    nav: [
      { text: '首页', link: '/' },
      { text: '学习路线', link: '/zh-CN/roadmap/' },
      {
        text: '学习资源',
          items: [
            {
              text: '第一个 Java 程序',
              link: '/zh-CN/guide/01-getting-started/01-first-java-program'
            },
            {
              text: '开发环境与 IntelliJ IDEA',
              link: '/zh-CN/guide/01-getting-started/02-development-environment-and-idea'
            },
            {
              text: '变量与数据类型',
              link: '/zh-CN/guide/01-getting-started/03-variables-and-data-types'
            },
            {
              text: '流程控制',
              link: '/zh-CN/guide/01-getting-started/04-control-flow'
            },
            {
              text: '方法',
              link: '/zh-CN/guide/01-getting-started/05-methods'
            },
            { text: 'Java 手册', link: '/zh-CN/handbook/README' },
          { text: '面试与复习', link: '/zh-CN/interview/README' }
        ]
      },
      { text: '写作规范', link: '/zh-CN/writing-guide' },
      { text: 'English', link: '/en/' }
    ],
    sidebar: {
      '/zh-CN/': [
        {
          text: '开始学习',
          items: [
            { text: '学习路线', link: '/zh-CN/roadmap/' },
            {
              text: '第一个 Java 程序',
              link: '/zh-CN/guide/01-getting-started/01-first-java-program'
            },
            {
              text: '开发环境与 IntelliJ IDEA',
              link: '/zh-CN/guide/01-getting-started/02-development-environment-and-idea'
            },
            {
              text: '变量与数据类型',
              link: '/zh-CN/guide/01-getting-started/03-variables-and-data-types'
            },
            {
              text: '流程控制',
              link: '/zh-CN/guide/01-getting-started/04-control-flow'
            },
            {
              text: '方法',
              link: '/zh-CN/guide/01-getting-started/05-methods'
            }
          ]
        },
        {
          text: '参考资料',
          items: [
            { text: 'Java 手册', link: '/zh-CN/handbook/README' },
            { text: '面试与复习', link: '/zh-CN/interview/README' },
            { text: '写作规范', link: '/zh-CN/writing-guide' }
          ]
        }
      ]
    }
  }
})
