import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Java 从零到精通',
  description: '以 Java 21 为基线的系统化 Java 学习项目',
  srcDir: '../docs',
  outDir: './.vitepress/dist',
  rewrites: {
    'en/README.md': 'en/index.md'
  },
  vite: {
    ssr: {
      external: ['vue', 'vue/server-renderer']
    },
    build: {
      rollupOptions: {
        external: ['vue', 'vue/server-renderer']
      }
    }
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
    locales: {
      root: {
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
              { text: 'Java 手册', link: '/zh-CN/handbook/README' },
              { text: '面试与复习', link: '/zh-CN/interview/README' }
            ]
          },
          { text: '写作规范', link: '/zh-CN/writing-guide' }
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
      },
      en: {
        nav: [{ text: 'English Home', link: '/en/' }],
        sidebar: {
          '/en/': [
            {
              text: 'English',
              items: [{ text: 'Overview', link: '/en/' }]
            }
          ]
        }
      }
    }
  }
})
